package org.alereno.json2rdf;

import com.jayway.jsonpath.*;
import org.alereno.json2rdf.Exceptions.IncompleteUriException;
import org.alereno.json2rdf.Exceptions.InvalidTripleMap;
import org.alereno.json2rdf.mapper.ObjectMap;
import org.alereno.json2rdf.mapper.Placeholder;
import org.alereno.json2rdf.mapper.Template;
import org.alereno.json2rdf.mapper.TriplesMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class Processor {

    private RmlModel rmlModel;
    private Configuration jsonPathConfiguration;

    public Processor(Model rmlModel) throws InvalidTripleMap {
        this.rmlModel = new RmlModel();
        this.rmlModel.loadModel(rmlModel);
    }

    public Model process(String json) {

        jsonPathConfiguration = Configuration.builder().options(Option.AS_PATH_LIST).build();
        ReadContext jsonContext = JsonPath.using(jsonPathConfiguration).parse(json);
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);

        Model model = ModelFactory.createDefaultModel();
        rmlModel.getTriplesMapsByUri().forEach((k, map) ->
                this.generateTriples(map, jsonContext, document, model));
        return model;
    }

    public List<Resource> generateTriples(TriplesMap triplesMap, ReadContext ctx, Object document, Model model) {

        List<Resource> subjectsCreated = new ArrayList<>();
        List<Object> documentElements;

        String iterator = triplesMap.getLogicalSourceMap().getIterator();
        try {
            // if the path is not in the data, we don't care, do nothing
            documentElements = ctx.read(iterator);
        }catch (PathNotFoundException e){
            return subjectsCreated;
        }

        log.debug("iterator --> {}", iterator);
        log.debug("{} elements to be created for {}", documentElements.size(), triplesMap.getResource());

        for (Object element : documentElements) {

            // Generate Subject Map
            try {

                String uriString = generateUriFormTemplate(triplesMap.getSubjectMap().getRrTemplate(), element, document);
                Resource subject = model.getResource(uriString);

                if (model.containsResource(subject)) {
                    log.info("{} was already created...", subject.getURI());
                    subjectsCreated.add(subject);
                    continue;
                }
                // Add types to subject
                for (Resource rrClass : triplesMap.getSubjectMap().getRrClasses()) {
                    model.add(subject, RDF.type, rrClass);
                }
                subjectsCreated.add(subject);
                // Add properties
                triplesMap.getPredicateObjectMap().forEach(predicateObjectMap -> {
                    ObjectMap objectMap = predicateObjectMap.getObjectMap();
                    Property property = model.createProperty(predicateObjectMap.getPredicateUri());

                    try {
                        switch (objectMap.getObjectMapType()) {
                            case DATA_TYPE:
                                RDFNode rdfNode;
                                String value  = getValueFromJson(element, document, objectMap.getReference());
                                if (objectMap.getDataType() == null) {
                                    log.warn("No dataType defined for property value {}, setting string", property.getURI());
                                    rdfNode = createRdfNode(value, XSDDatatype.XSDstring.getURI(), model);
                                } else
                                    rdfNode = createRdfNode(value, objectMap.getDataType().getURI(), model);
                                model.add(subject, property, rdfNode);
                                break;
                            case OBJECT_TYPE:
                                if (objectMap.getParentTriplesMap()!= null) {
                                    List<Resource> objects = generateTriples(objectMap.getParentTriplesMap(), ctx, document, model);
                                    // TODO: should I have here a RDFList instead of several triples??!!
                                    objects.stream().forEach(object -> model.add(subject, property, object));
                                } else if (objectMap.getTemplate() != null){
                                    String objectUri = generateUriFormTemplate(objectMap.getTemplate(), element, document);
                                    Resource object = model.getResource(objectUri);
                                    model.add(subject, property, object);
                                }
                                break;
                            case CONSTANT_DATA_TYPE:
                                if (objectMap.getDataType() == null) {
                                    log.warn("No dataType defined for property value {}, setting string", property.getURI());
                                    rdfNode = createRdfNode(objectMap.getConstantData(), XSDDatatype.XSDstring.getURI(), model);
                                } else
                                    rdfNode = createRdfNode(objectMap.getConstantData(), objectMap.getDataType().getURI(), model);
                                model.add(subject, property, rdfNode);
                                break;
                            case CONSTANT_OBJECT_TYPE:
                                model.add(subject, property, objectMap.getConstantObject());
                                break;
                            default:
                                log.error("No type defined for " + predicateObjectMap.getPredicateUri());
                                break;

                        }
                    } catch (Exception e) {
                        log.error(e.getMessage() + " at " + property.getURI(), e);
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);

            }
        }
        return subjectsCreated;

    }

    private String  getValueFromJson(Object element, Object document, String source) {
        String path = element + "." + source;
        String value = JsonPath.read(document, path).toString();
        return value;
    }

    private RDFNode createRdfNode(String value, String dataType, Model model){

        RDFNode rdfNode = null;

        if (dataType.equals(XSDDatatype.XSDstring.getURI()) || dataType == null) {
            rdfNode = model.createTypedLiteral(new String(value));

        } else if (dataType.equals(XSDDatatype.XSDboolean.getURI())) {
            rdfNode = model.createTypedLiteral(new Boolean(value));

        } else if (dataType.equals(XSDDatatype.XSDinteger.getURI()) ||
                dataType.equals(XSDDatatype.XSDint.getURI())) {
            rdfNode = model.createTypedLiteral(new Integer(value));

        } else if (dataType.equals(XSDDatatype.XSDfloat.getURI())) {
            rdfNode = model.createTypedLiteral(new Float(value));

        } else if (dataType.equals(XSDDatatype.XSDdouble.getURI())) {
            rdfNode = model.createTypedLiteral(new Double(value));

        } else if (dataType.equals(XSDDatatype.XSDdateTime.getURI())) {
            // TODO: implement date time casting

        } else if (dataType.equals(XSDDatatype.XSDdate.getURI())) {
            // TODO: implement date casting

        } else {
            log.error("Data type {} not identified", dataType);

        }
        return rdfNode;
    }

    private String generateUriFormTemplate(Template template, Object element, Object document) throws Exception {

        String uri;
        int expectedPlaceholders = template.getPlaceholders().size();
        HashMap<String, String> fieldsValues = new HashMap<>();

        for (Placeholder placeholder : template.getPlaceholders().values()) {
            String path = element + "." + placeholder.getSource();
            String value = JsonPath.read(document, path).toString();
            fieldsValues.put(placeholder.getSource(), URLEncoder.encode(value, "UTF-8"));
        }

        if (fieldsValues.size() == expectedPlaceholders) {
            uri = template.apply(fieldsValues);
        } else {
            String message = "Couldn't generate a uri, missing values, {}" + template.getPlaceholders().toString();
            throw new IncompleteUriException(message);
        }
        return uri;
    }

}

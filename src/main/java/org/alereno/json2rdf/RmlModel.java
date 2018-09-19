package org.alereno.json2rdf;

import be.mmlab.semweb.RML;
import org.alereno.json2rdf.Exceptions.InvalidTripleMap;
import org.alereno.json2rdf.mapper.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.*;
import org.w3c.vocabularies.R2RML;

import java.util.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * RmlModel class loads the RML model in different classes that reflect the R2RML vocabulary and RML extension.
 *
 */
@Slf4j
@Data
public class RmlModel {

    private Map<String, TriplesMap> triplesMapsByUri;
    private String namespace;

    public RmlModel() {
        this.triplesMapsByUri = new HashMap<>();
    }

    public void loadModel(Model rmlModel) throws InvalidTripleMap {


        StmtIterator iteratorTriplesMap = rmlModel.listStatements(null, RML.logicalSource, (RDFNode) null);

        if (iteratorTriplesMap.hasNext()) {

            while (iteratorTriplesMap.hasNext()) {

                Statement stmt = iteratorTriplesMap.nextStatement();

                TriplesMap triplesMap = new TriplesMap(stmt.getSubject().asResource());
                LogicalSourceMap logicalSourceMap = new LogicalSourceMap(stmt.getObject().asResource());
                log.debug("reading triplesMap -> {}", triplesMap.getResource());

                // get Logical Source
                StmtIterator iterator = logicalSourceMap.getResource().listProperties(RML.iterator);
                if (iterator.hasNext()) {

                    logicalSourceMap.setIterator(iterator.nextStatement()
                            .getObject()
                            .toString());
                    triplesMap.setLogicalSourceMap(logicalSourceMap);

                } else {
                    log.error("Missing RML.iterator");
                    throw new InvalidTripleMap("Missing RML.iterator");
                }

                // get the subjectsMap, templates and types
                iterator = triplesMap.getResource().listProperties(R2RML.subjectMap);

                if (iterator.hasNext()) {

                    SubjectMap subjectMap = new SubjectMap(iterator.nextStatement().getObject().asResource());
                    StmtIterator iteratorSubjectMapTemplate = subjectMap.getResource().listProperties(R2RML.template);

                    if (iteratorSubjectMapTemplate.hasNext()) {
                        Template template = createTemplate(iteratorSubjectMapTemplate.nextStatement().getObject()
                                .toString());
                        subjectMap.setRrTemplate(template);
                    }

                    StmtIterator iteratorSubjectMapClass = subjectMap.getResource().listProperties(R2RML.class_);
                    if (iteratorSubjectMapClass.hasNext()) {
                        while (iteratorSubjectMapClass.hasNext()) {
                            subjectMap.getRrClasses().add(iteratorSubjectMapClass.nextStatement()
                                    .getObject().asResource());
                        }
                    }

                    triplesMap.setSubjectMap(subjectMap);

                } else {
                    log.error("Missing R2RML.SubjectMap");
                    throw new InvalidTripleMap("Missing R2RML.SubjectMap");

                }

                iterator = triplesMap.getResource().listProperties(R2RML.predicateObjectMap);
                if (iterator.hasNext()) {

                    while (iterator.hasNext()) {

                        PredicateObjectMap predicateObjectMap =
                                new PredicateObjectMap(iterator.nextStatement().getObject().asResource());

                        StmtIterator predicateIterator = predicateObjectMap.getResource().listProperties(R2RML.predicate);

                        if (predicateIterator.hasNext()) {
                            predicateObjectMap.setPredicateUri(predicateIterator.nextStatement().getObject().asResource().getURI());
                        } else {
                            log.error("Missing R2RML.predicateObjectMap");
                            throw new InvalidTripleMap("Missing R2RML.predicateObjectMap");
                        }

                        StmtIterator objectMapIterator = predicateObjectMap.getResource().listProperties(R2RML.objectMap);

                        if (objectMapIterator.hasNext()) {
                            ObjectMap objectMap = new ObjectMap(objectMapIterator.nextStatement().getObject().asResource());

                            StmtIterator referenceIterator = objectMap.getResource().listProperties(RML.reference);
                            if (referenceIterator.hasNext()) {
                                objectMap.setReference(referenceIterator.nextStatement().getObject().asLiteral().getString());
                            }

                            StmtIterator dataTypeIterator = objectMap.getResource().listProperties(R2RML.datatype);
                            if (dataTypeIterator.hasNext()) {
                                objectMap.setDataType(dataTypeIterator.nextStatement().getObject().asResource());
                            }

                            StmtIterator constantIterator = objectMap.getResource().listProperties(R2RML.constant);
                            if (constantIterator.hasNext()) {

                                RDFNode object = constantIterator.nextStatement().getObject();
                                log.debug("WHAT IS IT {} ", object);
                                if(object.isResource())
                                    objectMap.setConstantObject(object.asResource());
                                else {

                                    objectMap.setConstantData(object.asLiteral().getString());
                                    log.debug("isConstant {} ",objectMap.isConstant());

                                }
                            }

                            StmtIterator parentTriplesIterator = objectMap.getResource().listProperties(R2RML.parentTriplesMap);
                            if (parentTriplesIterator.hasNext()) {
                                objectMap.setParentTriplesMapUri(parentTriplesIterator.nextStatement().getObject().asResource().getURI());
                            }


                            StmtIterator joinConditionIterator = objectMap.getResource().listProperties(R2RML.joinCondition);
                            if (joinConditionIterator.hasNext()) {
                                log.warn("RML.JoinCondition not supported!!");
                            }

                            predicateObjectMap.setObjectMap(objectMap);
                        } else {
                            log.error("Missing R2RML.objectMap");
                            throw new InvalidTripleMap("Missing R2RML.objectMap");
                        }
                        triplesMap.getPredicateObjectMap().add(predicateObjectMap);
                    }


                } else {
                    log.error("Missing RML.predicateObjectMap");
                    throw new InvalidTripleMap("Missing RML.predicateObjectMap");
                }

                if(namespace == null) this.namespace = triplesMap.getResource().getURI().split("#")[0] + "#";
                this.triplesMapsByUri.put(triplesMap.getResource().getURI(), triplesMap);

            }
        } else {
            log.error("Missing RML.LogicalSource");
            throw new InvalidTripleMap("Missing RML.LogicalSource");
        }

        List<String> children = new ArrayList<>();
        this.triplesMapsByUri.forEach((key, value) -> {
            value.getPredicateObjectMap().forEach(predicateObjectMap -> {
                if (predicateObjectMap.getObjectMap().getParentTriplesMapUri() != null) {
                    if (!triplesMapsByUri.containsKey(predicateObjectMap.getObjectMap().getParentTriplesMapUri())) {
                        log.error("Missing RML.getParentTriplesMap");
                    } else {
                        predicateObjectMap.getObjectMap().setParentTriplesMap(
                                triplesMapsByUri.get(predicateObjectMap.getObjectMap().getParentTriplesMapUri()));
                        children.add(predicateObjectMap.getObjectMap().getParentTriplesMapUri());
                    }

                }
            });
        });

        for(String child : children){
            triplesMapsByUri.remove(child);
        }
        log.debug("Top elements in hierarchy {}", triplesMapsByUri.keySet().toString());
        // TODO: what happen if I have no top elements in the hierarchy??

    }

    public static Template createTemplate(String templateString) {

        Template template = new Template(templateString);
        Pattern placeholderRegex = Pattern.compile("\\{(.*?)\\}");

        Matcher templatePlaceholders = placeholderRegex.matcher(template.getString());

        while (templatePlaceholders.find()) {

            String name = templatePlaceholders.group();
            Placeholder placeholder = Placeholder.builder()
                    .source(name.substring(1, name.length() - 1))
                    .startPosition(templatePlaceholders.start())
                    .endPosition(templatePlaceholders.end())
                    .build();
            template.getPlaceholders().put(placeholder.getSource(), placeholder);
        }

        return template;
    }
}

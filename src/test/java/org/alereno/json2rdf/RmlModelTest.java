package org.alereno.json2rdf;

import org.alereno.json2rdf.mapper.ObjectMap;
import org.alereno.json2rdf.mapper.PredicateObjectMap;
import org.alereno.json2rdf.mapper.Template;
import org.alereno.json2rdf.mapper.TriplesMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.jena.util.FileManager.get;
import static org.junit.Assert.assertTrue;

@Slf4j
public class RmlModelTest {

    @Test
    public void shouldLoadModel() throws Exception {

        InputStream in = get().open("store.rml");

        Model model = ModelFactory.createDefaultModel();
        model.read(in, null, "TTL");

        RmlModel rmlModel = new RmlModel();
        rmlModel.loadModel(model);

        assertTrue(rmlModel.getTriplesMapsByUri().size() == 2);

        String storeUri = rmlModel.getNamespace() + "Store";

        assertTrue(rmlModel.getTriplesMapsByUri()
                .get(storeUri)
                .getSubjectMap()
                .getRrTemplate()
                .getString()
                .equals("http://data.example.com/{identifier}"));

        List<String> classes = rmlModel.getTriplesMapsByUri()
                .get(storeUri)
                .getSubjectMap()
                .getRrClasses().stream().map((resource) -> resource.getURI()).collect(Collectors.toList());
        assertTrue(classes.size() == 2);
        assertTrue(classes.contains("http://schema.org/Business"));
        assertTrue(classes.contains("http://schema.org/Organization"));

        assertTrue(rmlModel.getTriplesMapsByUri()
                .get(storeUri)
                .getPredicateObjectMap().size() == 3);

        PredicateObjectMap sellsPredicate = rmlModel.getTriplesMapsByUri()
                .get(storeUri)
                .getPredicateObjectMap().get(0);

        assertTrue(sellsPredicate.getPredicateUri()
                .equals("http://schema.org/sells"));

        assertTrue(sellsPredicate.getObjectMap().getParentTriplesMap() != null);

        assertTrue(sellsPredicate.getObjectMap().getParentTriplesMapUri()
                .endsWith("#Book"));
        assertTrue(sellsPredicate.getObjectMap().isConstant() == false);
        assertTrue(sellsPredicate.getObjectMap().getReference() == null);


        rmlModel.getTriplesMapsByUri()
                .get(storeUri)
                .getPredicateObjectMap()
                .forEach((predicateObjectMap) -> {

                    switch (predicateObjectMap.getPredicateUri()) {

                        case "http://schema.org/name":
                            ObjectMap objectMap = predicateObjectMap.getObjectMap();
                            assertTrue(objectMap.getReference().equals("name"));
                            assertTrue(objectMap.isConstant() == false);
                            assertTrue(objectMap.getObjectMapType() == ObjectMap.ObjectMapType.DATA_TYPE);
                            assertTrue(objectMap.getDataType().getURI().toString().equals(XSDDatatype.XSDstring.getURI().toString()));

                            break;
                        case "http://schema.org/location":
                            objectMap = predicateObjectMap.getObjectMap();
                            assertTrue(objectMap.getReference() == null);
                            assertTrue(objectMap.isConstant() == true);
                            assertTrue(objectMap.getObjectMapType() == ObjectMap.ObjectMapType.CONSTANT_OBJECT_TYPE);
                            assertTrue(objectMap.getParentTriplesMapUri() == null);
                            assertTrue(objectMap.getParentTriplesMap() == null);
                            assertTrue(objectMap.getConstantObject().getURI().toString().equals("http://example.org/China"));
                            assertTrue(objectMap.getDataType() == null);

                            break;
//                        case "http://schema.org/verified":
//                            objectMap = predicateObjectMap.getObjectMap();
//                            log.debug(objectMap.toString());
//                            assertTrue(objectMap.getReference()== null);
//                            assertTrue(objectMap.isConstant() == true);
//                            assertTrue(objectMap.getConstantData().equals("true"));
//                            assertTrue(objectMap.getParentTriplesMapUri()==null);
//                            assertTrue(objectMap.getParentTriplesMap()==null);
//                            assertTrue(objectMap.getObjectMapType() == ObjectMap.ObjectMapType.CONSTANT_DATA_TYPE);
//                            assertTrue(objectMap.getDataType().getURI().equals(XSDDatatype.XSDboolean.getURI().toString()));
//                            break;
                        case "http://schema.org/sells":
                            objectMap = predicateObjectMap.getObjectMap();

                            assertTrue(objectMap.getReference() == null);
                            assertTrue(objectMap.isConstant() == false);
                            assertTrue(objectMap.getObjectMapType() == ObjectMap.ObjectMapType.OBJECT_TYPE);
                            assertTrue(objectMap.getParentTriplesMapUri().equals(rmlModel.getNamespace() + "Book"));
                            assertTrue(objectMap.getParentTriplesMap() != null);
                            assertTrue(objectMap.getDataType() == null);
                            TriplesMap bookTriplesMap = objectMap.getParentTriplesMap();

                            bookTriplesMap.getPredicateObjectMap().forEach((booksPredicateObjectMap) -> {

                                switch (booksPredicateObjectMap.getPredicateUri()) {

                                    case "http://schema.org/status":
                                        ObjectMap booksObjectMap = booksPredicateObjectMap.getObjectMap();
                                        assertTrue(booksObjectMap.getReference() == null);
                                        assertTrue(booksObjectMap.isConstant() == true);
                                        assertTrue(booksObjectMap.getObjectMapType() == ObjectMap.ObjectMapType.CONSTANT_DATA_TYPE);
                                        assertTrue(booksObjectMap.getParentTriplesMap() == null);
                                        assertTrue(booksObjectMap.getDataType() != null);
                                        assertTrue(booksObjectMap.getConstantData().equals("soldOut"));
                                        break;
                                    default:
                                        break;

                                }
                            });
                            break;


                    }

                });

    }

    @Test
    public void createTemplateTests() {

        String aTemplate = "http://example.org/{onePlaceholder}";
        Template template = RmlModel.createTemplate(aTemplate);
        assertTrue(template.getString().equals(aTemplate));
        assertTrue(template.getPlaceholders().size() == 1);

        assertTrue(template.getPlaceholders().get("onePlaceholder").getSource().equals("onePlaceholder"));

        assertTrue(template.getPlaceholders().get("onePlaceholder").getStartPosition() == 19);
        assertTrue(template.getPlaceholders().get("onePlaceholder").getEndPosition() == 35);

        template = RmlModel.createTemplate("http://example.org/noplaceholder");
        assertTrue(template.getPlaceholders().size() == 0);

        template = RmlModel.createTemplate("http://example.org/{first}and{second}");
        assertTrue(template.getPlaceholders().size() == 2);
        assertTrue(template.getPlaceholders().get("first").getSource().equals("first"));
        assertTrue(template.getPlaceholders().get("second").getSource().equals("second"));
    }

}

package org.alereno.json2rdf.mapper;


import lombok.Data;
import org.apache.jena.rdf.model.Resource;

@Data
public class PredicateObjectMap extends Map{

    private String predicateUri;
    private ObjectMap objectMap;

    public PredicateObjectMap(Resource resource) {
        super(resource);
    }

}

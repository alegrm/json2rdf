package org.alereno.json2rdf.mapper;

import lombok.Data;
import org.apache.jena.rdf.model.Resource;
import java.util.ArrayList;

@Data
public class TriplesMap extends Map {

    private SubjectMap subjectMap;
    private LogicalSourceMap logicalSourceMap;
    private ArrayList<PredicateObjectMap> predicateObjectMap;
    
    public TriplesMap(Resource resource) {
        super(resource);
        this.predicateObjectMap = new ArrayList<>();
    }
}

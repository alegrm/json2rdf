package org.alereno.json2rdf.mapper;
import lombok.Data;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubjectMap extends Map{

    private Template rrTemplate;
    private List<Resource> rrClasses;

    public SubjectMap(Resource resource) {
        super(resource);
        this.rrClasses = new ArrayList<>();
    }
}

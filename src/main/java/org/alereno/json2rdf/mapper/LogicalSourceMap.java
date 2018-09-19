package org.alereno.json2rdf.mapper;

import lombok.Data;
import org.apache.jena.rdf.model.Resource;

@Data
public class LogicalSourceMap extends Map{

    private String iterator;

    public LogicalSourceMap(Resource resource){
        super(resource);
    }


}

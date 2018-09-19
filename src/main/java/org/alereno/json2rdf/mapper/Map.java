package org.alereno.json2rdf.mapper;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.jena.rdf.model.Resource;

@Data
@RequiredArgsConstructor
public class Map {

    @NonNull
    private Resource resource;

}

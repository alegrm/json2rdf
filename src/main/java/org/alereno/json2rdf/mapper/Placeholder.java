package org.alereno.json2rdf.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Placeholder{

    @NonNull
    private String source;
    @NonNull
    private int startPosition;
    @NonNull
    private int endPosition;


    public String getAbsolutePath(String iterator){
        return iterator + source;
    }

}
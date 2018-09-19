package org.alereno.json2rdf.mapper;

import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

@Data
public class Template {

    @NonNull
    private String string;
    private HashMap<String, Placeholder> placeholders;

    public Template(String string){
        this.string = string;
        placeholders = new HashMap<>();
    }

    public String apply(HashMap<String,String> values){
        String uri = this.getString();

        for(Map.Entry<String, String> entry : values.entrySet()){
            Placeholder placeholder = this.getPlaceholders().get(entry.getKey());
            uri = uri.replace(
                    this.getString().substring(placeholder.getStartPosition(), placeholder.getEndPosition()),
                    entry.getValue());
        }

        return uri;
    }
}

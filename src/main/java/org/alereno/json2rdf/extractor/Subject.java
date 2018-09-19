package org.alereno.json2rdf.extractor;

import org.alereno.json2rdf.mapper.Template;
import lombok.Data;

import java.util.List;

@Data
public class Subject {

    private List<Field> fieldList;
    private int index;
    private Template template;


}

package org.alereno.json2rdf.extractor;

import lombok.Data;


/**
 * The Field is the absolute path, I mean, it includes the iterator
 */

@Data
public class Field {

    private String name;
    private Object value;

}

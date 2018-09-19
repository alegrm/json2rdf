package org.alereno.json2rdf.mapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;

@Slf4j
@Data
public class ObjectMap extends Map {

    private String reference;
    private String constantData;
    private Resource constantObject;
    private Resource dataType;
    private ObjectMapType objectMapType;

    private String parentTriplesMapUri;
    private TriplesMap parentTriplesMap;
    private Template template;

    public ObjectMap(Resource resource) {
        super(resource);
    }

    public boolean isConstant() {
        return (constantData != null || constantObject != null);
    }

    public ObjectMapType getObjectMapType() {

        ObjectMapType type = null;
        if (reference != null && isConstant() == false && parentTriplesMap == null)
            type = ObjectMapType.DATA_TYPE;
        else if (isConstant() == true && reference == null && parentTriplesMap == null) {
            if (constantObject == null)
                type = ObjectMapType.CONSTANT_DATA_TYPE;
            else
                type = ObjectMapType.CONSTANT_OBJECT_TYPE;

        } else if (parentTriplesMapUri != null && reference == null && isConstant() == false)
            type = ObjectMapType.OBJECT_TYPE;

        return type;
    }

    public enum ObjectMapType {
        OBJECT_TYPE, CONSTANT_OBJECT_TYPE, CONSTANT_DATA_TYPE, DATA_TYPE
    }

}

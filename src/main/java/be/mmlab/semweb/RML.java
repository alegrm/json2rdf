/* CVS $Id: $ */
package be.mmlab.semweb; 
import org.apache.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from src/main/resources/rml.ttl 
 * @author Auto-generated by schemagen on 17 mai 2018 22:25 
 */
public class RML {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static final Model M_MODEL = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://semweb.mmlab.be/ns/rml#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     * @return namespace as String
     * @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = M_MODEL.createResource( NS );
    
    public static final Property iterator = M_MODEL.createProperty( "http://semweb.mmlab.be/ns/rml#iterator" );
    
    public static final Property logicalSource = M_MODEL.createProperty( "http://semweb.mmlab.be/ns/rml#logicalSource" );
    
    public static final Property query = M_MODEL.createProperty( "http://semweb.mmlab.be/ns/rml#query" );
    
    public static final Property reference = M_MODEL.createProperty( "http://semweb.mmlab.be/ns/rml#reference" );
    
    public static final Property referenceFormulation = M_MODEL.createProperty( "http://semweb.mmlab.be/ns/rml#referenceFormulation" );
    
    public static final Property version = M_MODEL.createProperty( "http://semweb.mmlab.be/ns/rml#version" );
    
    public static final Resource BaseSource = M_MODEL.createResource( "http://semweb.mmlab.be/ns/rml#BaseSource" );
    
    public static final Resource LogicalSource = M_MODEL.createResource( "http://semweb.mmlab.be/ns/rml#LogicalSource" );
    
    public static final Resource ReferenceFormulation = M_MODEL.createResource( "http://semweb.mmlab.be/ns/rml#ReferenceFormulation" );
    
}

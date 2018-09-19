# RML-Mapper for JSON streams


1. Loads in memory the rml model
2. Generates the subjects with defined URI templates
3. Add RDF.types to the subjects
4. Generate properties


## URI template

The URI of a resource is defined by an string with the possibility of adding data values using placeholders 
delimited by curly brackets: {}

    rr:subjectMap [
        rr:template "http://example.com/book/{bookid}" ;
        rr:class owl:Thing
    ];
    
## Using scripting templates


Creating a new uuid

    rr:subjectMap [
        rr:template "http://example.com/book/{{uuid.generate()}}" ;
        rr:class owl:Thing
    ];
    

Use iteration index


    rml:logicalSource [
        rml:source "source.json";
        rml:referenceFormulation ql:JSONPath;
        rml:iterator "$.store.books.[*]"
      ];
      
   
    rr:subjectMap [
        rr:template "http://example.com/book/{{}}" ;
        rr:class kso:DnsRequest
    ];



Ideas for templating


    rr:subjectMap [
        rr:template "http://example.com/book/{{uuid.generate()}}" ;
        rr:class owl:Thing
    ];
    
    
//    Eval javascript inside java
//    https://docs.oracle.com/javase/8/docs/api/javax/script/ScriptEngine.html
//    ScriptEngineManager manager = new ScriptEngineManager();
//    ScriptEngine engine = manager.getEngineByName("js");
//    Object result = engine.eval("4*5");

## TODO

* Add RML.joinCondition support
* RDF.List as Data Type
* Script Engine
* Add generated sources to target directory
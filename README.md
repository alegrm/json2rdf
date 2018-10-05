# JSON to RDF
[![Build Status](https://travis-ci.com/alegrm/json2rdf.svg?branch=master)](https://travis-ci.com/alegrm/json2rdf)

WORK-IN-PROGRESS

This library transforms JSON data to RDF using a [RML](http://rml.io) mapping file. 

1. Loads in memory the rml model
2. Generates the subjects with defined URI templates
3. Add RDF.types to the subjects
4. Generate properties


## Use

This application implements the [RML](http://rml.io) specification. To learn how to create RML flies please go to the Specs section.

A fat jar is packaged to be used as a java executable:

        $ java -jar json2rdf-0.1.0-jar-with-dependencies.jar
        Usage: <main class> [options]
          Options:
            --help, -h
        
          * --inputFile, -f
              JSON file to convert
          * --rml, -m
              RML mapping file



## TODO


* Add RML.joinCondition support
* RDF.List as Data Type
* Script Engine
* Add generated sources to target directory
* Add the project to maven central
    
### Future work
#### Using scripting templates
    

Use iteration index to create consecutive URI


    rml:logicalSource [
        rml:source "source.json";
        rml:referenceFormulation ql:JSONPath;
        rml:iterator "$.store.books.[*]"
      ];
    rr:subjectMap [
        rr:template "http://example.com/book/{{}}" ;
        rr:class kso:DnsRequest
    ];



Javascript 


    rr:subjectMap [
        rr:template "http://example.com/book/{{uuid.generate()}}" ;
        rr:class owl:Thing
    ];
    
      rr:predicateObjectMap [
        rr:predicate schema:title;
        rr:objectMap [
           rml:reference "title" ;
           rr:datatype xsd:string
           rml:script: ""
        ];
      ];
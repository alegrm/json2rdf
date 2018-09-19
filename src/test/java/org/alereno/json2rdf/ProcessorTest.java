package org.alereno.json2rdf;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.*;
import org.junit.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.jena.util.FileManager.get;
import static org.junit.Assert.assertTrue;

@Slf4j
public class ProcessorTest {

    @Test
    public void storeTest() throws Exception {

        InputStream in = get().open("store.rml");

        Model rmlModel = ModelFactory.createDefaultModel();
        rmlModel.read(in, null, "TTL");
        Processor processor = new Processor(rmlModel);
        String jsonString = new String(Files.readAllBytes(Paths.get("src", "test", "resources", "store.json")));

        Model model = processor.process(jsonString);

        StmtIterator  stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()){
            Statement statement = stmtIterator.nextStatement();
            log.debug(statement.toString());
        }

        Resource store = model.getResource("http://data.example.com/ABCD123");
        Property name = model.getProperty("http://schema.org/name");
        assertTrue(model.contains(store, name));
    }

    @Test
    public void museumTest() throws Exception {

        InputStream in = get().open("museum.rml");

        Model rmlModel = ModelFactory.createDefaultModel();
        rmlModel.read(in, null, "TTL");
        Processor processor = new Processor(rmlModel);
        String jsonString = new String(Files.readAllBytes(Paths.get("src", "test", "resources", "museum.json")));

        Model model = processor.process(jsonString);

        StmtIterator  stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()){
            Statement statement = stmtIterator.nextStatement();
            log.debug(statement.toString());
        }


    }

    @Test
    public void venueTest() throws Exception {

        InputStream in = get().open("venue.rml");

        Model rmlModel = ModelFactory.createDefaultModel();
        rmlModel.read(in, null, "TTL");
        Processor processor = new Processor(rmlModel);
        String jsonString = new String(Files.readAllBytes(Paths.get("src", "test", "resources", "venue.json")));

        Model model = processor.process(jsonString);

        StmtIterator  stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()){
            Statement statement = stmtIterator.nextStatement();
            log.debug(statement.toString());
        }


    }

    @Test
    public void scriptEngineTests() throws ScriptException, NoSuchMethodException {

        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine("--language=es6");

        // evaluate JavaScript code from String
        engine.eval("print('Hello, World')");

        // JavaScript code in a String
        String script = "function hello(name) { print('Hello, ' + name); }";
        // evaluate script
        engine.eval(script);

        // javax.script.Invocable is an optional interface.
        // Check whether your script engine implements or not!
        // Note that the JavaScript engine implements Invocable interface.
        Invocable inv = (Invocable) engine;

        // invoke the global function named "hello"
        inv.invokeFunction("hello", "Scripting!!");

        //Template strings
//        engine.eval("var name = 'Sanaulla'");
//        engine.eval("print(`Hello Mr. ${name}`)");

    }
}

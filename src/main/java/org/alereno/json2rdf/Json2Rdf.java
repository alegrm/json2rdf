package org.alereno.json2rdf;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.jena.util.FileManager.get;

/**
 *
 *
 */
@Slf4j
public class Json2Rdf {

    @Parameter(names = {"--rml", "-m"}, required = true, description = "RML mapping file")
    private String mappingFile;
    @Parameter(names = {"--inputFile", "-f"}, required = true, description = "JSON file to convert")
    private String inputFile;

    @Parameter(names ={"--help", "-h"}, help = true)
    private boolean help;

    public void run() {
        log.info("mapping {}, file {}", mappingFile, inputFile);

        try {

            InputStream in = get().open(mappingFile);

            Model rmlModel = ModelFactory.createDefaultModel();
            rmlModel.read(in, null, "TTL");

            Processor processor = new Processor(rmlModel);
            String jsonString = new String(Files.readAllBytes(Paths.get(inputFile)));
            Model model = processor.process(jsonString);

            String outputFile = inputFile + ".ttl";
            log.info("writing output {}", outputFile);

            OutputStream out = new FileOutputStream(outputFile);
            model.write(out, "TTL");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        Json2Rdf json2Rdf = new Json2Rdf();

        JCommander jc = JCommander.newBuilder()
                .addObject(json2Rdf)
                .build();
        try {
            jc.parse(args);
            json2Rdf.run();

        } catch (ParameterException pe) {
            jc.usage();
        }

    }
}

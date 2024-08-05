import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*class CertificateGen {

    public static List<String> Generate(String svgTemplateFile, String DataFile) {
        List<String> generatedFiles = new ArrayList<>();
        try {
            String svgTemplate = new String(Files.readAllBytes(Paths.get(svgTemplateFile)));
            Files.createDirectories(Paths.get("certificates"));

            BufferedReader br = new BufferedReader(new FileReader(DataFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String name = values[0];
                String course = values[1];
                String date = values[2];

                String modifiedSvg = svgTemplate.replace("id=\"svgName\">name", "id=\"svgName\">" + name)
                        .replace("id=\"svgCourse\">course", "id=\"svgCourse\">" + course)
                        .replace("id=\"svgDate\">date", "id=\"svgDate\">" + date);

                String outputFileName = "certificates/" + name + ".png";
                convertSvgToPng(modifiedSvg, outputFileName);
                generatedFiles.add(name + ".png");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generatedFiles;
    }

    private static void convertSvgToPng(String svgContent, String outputPath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(svgContent)));

        PNGTranscoder transcoder = new PNGTranscoder();
        TranscoderInput input = new TranscoderInput(document);
        try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
        }
    }
}*/

public class CertificateGen{

    public static List<String> Generate(String template, String csvFilePath) {
        
        System.out.println("Here 1");

        List<String> generatedFiles = new ArrayList<>();
        
        if (template.equalsIgnoreCase("certificate_template1.svg")) {

            try {
                String svgTemplate = new String(Files.readAllBytes(Paths.get(template)));
                Files.createDirectories(Paths.get("certificates"));

                BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    String name = values[0];
                    String course = values[1];
                    String date = values[2];

                    String modifiedSvg = svgTemplate.replace("id=\"svgName\">name", "id=\"svgName\">" + name)
                            .replace("id=\"svgCourse\">course", "id=\"svgCourse\">" + course)
                            .replace("id=\"svgDate\">date", "id=\"svgDate\">" + date);

                    String outputFileName = "certificates/" + name + ".png";
                    convertSvgToPng(modifiedSvg, outputFileName);
                    generatedFiles.add(name + ".png");
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return generatedFiles;
        } 
        else if (template.equalsIgnoreCase("certificate_template2.svg")) {
            
            try {
                // Read the SVG template file
                String svgTemplate = new String(Files.readAllBytes(Paths.get(template)));
                
                // Ensure the output directory exists
                Files.createDirectories(Paths.get("certificates"));

                // Read the data file
                BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
                String line;
                while ((line = br.readLine()) != null) {
                    // Parse the CSV line
                    String[] values = line.split(",");
                    String name = values[0];
                    String rank = values[1];  // Adjust based on your CSV structure
                    String event = values[2]; // Adjust based on your CSV structure

                    // Replace placeholders in the SVG template
                    String modifiedSvg = svgTemplate
                        .replace("id=\"name\">Name", "id=\"name\">" + name)
                        .replace("id=\"rank\">Rank", "id=\"rank\">" + rank)
                        .replace("id=\"event\">Event", "id=\"event\">" + event);

                    // Define output file name
                    String outputFileName = "certificates/" + name + ".png";

                    // Convert SVG to PNG
                    convertSvgToPng(modifiedSvg, outputFileName);

                    // Add to generated files list
                    generatedFiles.add(name + ".png");
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return generatedFiles;
        }

        else if (template.equalsIgnoreCase("certificate_template3.svg")) {

            System.out.println("Here 2");
            
            try {
                // Read the SVG template file
                String svgTemplate = new String(Files.readAllBytes(Paths.get(template)));
                
                // Ensure the output directory exists
                Files.createDirectories(Paths.get("certificates"));

                // Read the data file
                BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
                String line;
                while ((line = br.readLine()) != null) {
                    // Parse the CSV line
                    String[] values = line.split(",");
                    String name = values[0];
                    String event = values[1]; // Adjust based on your CSV structure

                    // Replace placeholders in the SVG template
                    String modifiedSvg = svgTemplate
                        .replace("id=\"name\">Name", "id=\"name\">" + name)
                        .replace("id=\"event\">Event", "id=\"event\">" + event);

                    // Define output file name
                    String outputFileName = "certificates/" + name + ".png";

                    // Convert SVG to PNG
                    convertSvgToPng(modifiedSvg, outputFileName);

                    // Add to generated files list
                    generatedFiles.add(name + ".png");
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return generatedFiles;
        }
        
        return generatedFiles;
    } 

    private static void convertSvgToPng(String svgContent, String outputPath) throws Exception {
        System.out.println("Here 3");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(svgContent)));

        PNGTranscoder transcoder = new PNGTranscoder();
        TranscoderInput input = new TranscoderInput(document);
        try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
        }
    }
}
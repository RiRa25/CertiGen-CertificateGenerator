import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SimpleHttpServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(4002), 0);
        server.createContext("/", new MyHttpHandler());
        server.createContext("/certificates", new CertificateHandler());
        server.createContext("/certificates/download", new CertificateDownloadHandler());
        server.createContext("/certificates/download_all", new CertificateDownloadAllHandler());
        server.createContext("/static", new StaticFileHandler("static"));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:4002/");
    }
}

class MyHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
            handleGetRequest(httpExchange);
        } else if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handlePostRequest(httpExchange);
        }
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        Path filePath = Paths.get("upload.html");
        byte[] response = Files.readAllBytes(filePath);

        httpExchange.sendResponseHeaders(200, response.length);
        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String contentType = httpExchange.getRequestHeaders().getFirst("Content-Type");
    
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            String boundary = "--" + contentType.split("boundary=")[1];
            InputStream inputStream = httpExchange.getRequestBody();
            Path uploadsDir = Paths.get("uploads");
            Files.createDirectories(uploadsDir);
    
            byte[] buffer = new byte[8192];
            int bytesRead;
            boolean isFilePart = false;
            String filename = null;
            String template = null; // Variable to store selected template
    
            ByteArrayOutputStream fileOutputStream = null;
            String fileSavedAs = null;
    
            try {
                ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bufferStream.write(buffer, 0, bytesRead);
                }
                byte[] data = bufferStream.toByteArray();
    
                String dataString = new String(data, StandardCharsets.UTF_8);
                String[] parts = dataString.split(boundary);
    
                for (String part : parts) {
                    if (part.contains("Content-Disposition: form-data; name=\"file\"")) {
                        String[] lines = part.split("\r\n");
                        for (String line : lines) {
                            if (line.startsWith("Content-Disposition")) {
                                int start = line.indexOf("filename=\"") + 10;
                                int end = line.indexOf("\"", start);
                                filename = line.substring(start, end);
                                filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
                                fileOutputStream = new ByteArrayOutputStream();
                            } else if (line.isEmpty() && fileOutputStream != null) {
                                isFilePart = true;
                            } else if (isFilePart && fileOutputStream != null) {
                                fileOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
                                fileOutputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
                            }
                        }
    
                        if (fileOutputStream != null) {
                            Path savedFilePath = Paths.get(uploadsDir.toString(), filename);
                            Files.write(savedFilePath, fileOutputStream.toByteArray());
                            fileSavedAs = savedFilePath.toAbsolutePath().toString();
                            fileOutputStream.close();
                            fileOutputStream = null;
                        }
                    } else if (part.contains("Content-Disposition: form-data; name=\"template\"")) {
                        String[] lines = part.split("\r\n");
                        for (String line : lines) {
                            if (line.endsWith(".svg")) {
                                template = line;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
    
            if (template == null) {
                String response = "<html><body><h1>Template Not Selected</h1>" +
                                  "<p>Please go back and select a template.</p>" +
                                  "<a href=\"/\">Go Back</a></body></html>";
                httpExchange.sendResponseHeaders(302, response.length()); // Redirect or send error page
                try (OutputStream outputStream = httpExchange.getResponseBody()) {
                    outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }
            
            System.out.println(template);
            System.out.println("meow " + fileSavedAs);
    
            List<String> generatedFiles = CertificateGen.Generate(template, fileSavedAs);
    
            StringBuilder response = new StringBuilder("<html><body><h1>Files uploaded successfully</h1>");
            for (String generatedFile : generatedFiles) {
                response.append("<div><img src=\"/certificates/")
                        .append(generatedFile)
                        .append("\" alt=\"Certificate\"><br><a href=\"/certificates/download/")
                        .append(generatedFile)
                        .append("\">Download Certificate</a></div><br>");
            }
            response.append("<br><a href=\"/certificates/download_all\">Download All Certificates</a>");
            response.append("<br><a href=\"/\">Upload another file</a>");
            response.append("</body></html>");
    
            byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
            httpExchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        } else {
            String response = "Invalid request";
            httpExchange.sendResponseHeaders(400, response.length());
            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
    
}
class CertificateHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
            handleGetRequest(httpExchange);
        }
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        Path certificatesDir = Paths.get("certificates");
        StringBuilder response = new StringBuilder();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(certificatesDir, "*.png")) {
            response.append("<html><body><h1>Certificates</h1><ul>");
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                response.append("<li><a href=\"/certificates/download/")
                        .append(fileName)
                        .append("\">")
                        .append(fileName)
                        .append("</a></li>");
            }
            response.append("</ul></body></html>");
        }

        byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}

class CertificateDownloadHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
            handleGetRequest(httpExchange);
        }
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        String requestedFile = httpExchange.getRequestURI().getPath().replace("/certificates/download/", "");
        Path filePath = Paths.get("certificates", requestedFile);

        if (Files.exists(filePath)) {
            byte[] bytes = Files.readAllBytes(filePath);
            httpExchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + requestedFile);
            httpExchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        } else {
            String response = "File not found.";
            httpExchange.sendResponseHeaders(404, response.length());

            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}

class CertificateDownloadAllHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
            handleGetRequest(httpExchange);
        }
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        Path certificatesDir = Paths.get("certificates");
        Path zipPath = Paths.get("certificates.zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(certificatesDir, "*.png")) {
                for (Path entry : stream) {
                    zos.putNextEntry(new ZipEntry(entry.getFileName().toString()));
                    Files.copy(entry, zos);
                    zos.closeEntry();
                }
            }
        }

        if (Files.exists(zipPath)) {
            byte[] bytes = Files.readAllBytes(zipPath);
            httpExchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + zipPath.getFileName().toString());
            httpExchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        } else {
            String response = "No certificates found.";
            httpExchange.sendResponseHeaders(404, response.length());

            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}

class StaticFileHandler implements HttpHandler {
    private final Path rootDirectory;

    public StaticFileHandler(String rootDirectory) {
        this.rootDirectory = Paths.get(rootDirectory);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String requestPath = httpExchange.getRequestURI().getPath();
        Path filePath = rootDirectory.resolve(requestPath.substring("./static".length()));

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            httpExchange.getResponseHeaders().set("Content-Type", contentType);

            byte[] fileBytes = Files.readAllBytes(filePath);
            httpExchange.sendResponseHeaders(200, fileBytes.length);

            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(fileBytes);
            }
        } else {
            String response = "File not found.";
            httpExchange.sendResponseHeaders(404, response.length());

            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}

# CertiGen

CertiGen is a simple HTTP server that generates certificates from uploaded data and serves static files. The project provides an easy way to upload data and generate certificates based on predefined templates.

## Project Structure

```plaintext
CertiGen/
│
├── static/
│   └── (Static files served by the server)
│       ├── template1.png
│       ├── template2.png
│       └── template3.png
│
├── CertificateGen.java
│   └── (Java class responsible for generating certificates)
│
├── SimpleHttpServer.java
│   └── (Java class to set up and run the HTTP server)
│
├── certificate_template1.svg
├── certificate_template2.svg
├── certificate_template3.svg
│   └── (SVG templates for certificate generation)
│
├── org.zip
│   └── (Contains necessary JAR files for Apache Batik and XML parsing)
│
├── upload.html
│   └── (HTML form to upload data for certificate generation)
│

package bpc;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Initializing Fuseki Server...");

            // Load the 2015 dataset
            System.out.println("Loading 2015 HDT dataset...");
            HDT hdt2015 = HDTManager.mapIndexedHDT("dbpedia_2015/dbpedia_2015.hdt");
            HDTGraph graph2015 = new HDTGraph(hdt2015, true);
            Model model2015 = ModelFactory.createModelForGraph(graph2015);
            Dataset dataset2015 = DatasetFactory.wrap(model2015);

            // Load the 2022 dataset
            System.out.println("Loading 2022 HDT dataset...");
            HDT hdt2022 = HDTManager.mapIndexedHDT("dbpedia_2022/dbpedia_2022.hdt");
            HDTGraph graph2022 = new HDTGraph(hdt2022, true);
            Model model2022 = ModelFactory.createModelForGraph(graph2022);
            Dataset dataset2022 = DatasetFactory.wrap(model2022);

            // Set up the Fuseki server with a health check endpoint
            System.out.println("Configuring Fuseki Server...");
            FusekiServer server = FusekiServer.create()
                    .add("/dbpedia2015", dataset2015) // SPARQL endpoint for 2015 dataset
                    .add("/dbpedia2022", dataset2022) // SPARQL endpoint for 2022 dataset
                    .enableCors(true) // Enable CORS for tools like YASGUI
                    .port(3330) // Use port 3330
                    .addServlet("/status", new HttpServlet() {
                        @Override
                        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.setContentType("text/plain");
                            resp.getWriter().write("Server is running!");
                        }
                    }) // Add a health check servlet
                    .build();

            System.out.println("Starting Fuseki Server...");
            server.start();
            System.out.println("Fuseki Server is running!");

            // Print out the available endpoints
            System.out.println("Available SPARQL Endpoints:");
            System.out.println("2015 Dataset: http://localhost:3330/dbpedia2015");
            System.out.println("2022 Dataset: http://localhost:3330/dbpedia2022");
            System.out.println("Health Check: http://localhost:3330/status");

        } catch (IOException e) {
            System.err.println("Error while loading HDT files or starting the server:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("General error occurred:");
            e.printStackTrace();
        }
    }
}

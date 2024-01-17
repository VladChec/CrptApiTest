import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final HttpClient httpClient;
    private final Semaphore requestSemaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.requestSemaphore = new Semaphore(requestLimit);

        Runnable releasePermitTask = () -> {
            while (true) {
                try {
                    Thread.sleep(timeUnit.toMillis(1));
                    requestSemaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(releasePermitTask).start();
    }

    public void createDocument(Document document, String signature) {
        try {
            requestSemaphore.acquire();
            String requestBody = buildRequestBody(document, signature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("API Response: " + response.body());
        } catch (InterruptedException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private String buildRequestBody(Document document, String signature) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestBody = objectMapper.createObjectNode();

        ObjectNode descriptionNode = objectMapper.createObjectNode();
        descriptionNode.put("participantInn", "string");

        requestBody.set("description", descriptionNode);
        requestBody.put("doc_id", "string");
        requestBody.put("doc_status", "string");
        requestBody.put("doc_type", "LP_INTRODUCE_GOODS");
        requestBody.put("importRequest", true);
        requestBody.put("owner_inn", "string");
        requestBody.put("participant_inn", "string");
        requestBody.put("producer_inn", "string");
        requestBody.put("production_date", "2020-01-23");
        requestBody.put("production_type", "string");

        ObjectNode productNode = objectMapper.createObjectNode();
        productNode.put("certificate_document", "string");
        productNode.put("certificate_document_date", "2020-01-23");
        productNode.put("certificate_document_number", "string");
        productNode.put("owner_inn", "string");
        productNode.put("producer_inn", "string");
        productNode.put("production_date", "2020-01-23");
        productNode.put("tnved_code", "string");
        productNode.put("uit_code", "string");
        productNode.put("uitu_code", "string");

        requestBody.putArray("products").add(productNode);

        requestBody.put("reg_date", "2020-01-23");
        requestBody.put("reg_number", "string");

        return requestBody.toString();
    }

    private static class Document {

        @Override
        public String toString() {
            return "{\"field1\": \"value1\", \"field2\": \"value2\"}";
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);

        Document document = new Document();
        String signature = "sampleSignature";

        crptApi.createDocument(document, signature);
    }
}

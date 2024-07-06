package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private final TimeUnit timeUnit;

    private final int requestLimit;
    private final long start = System.currentTimeMillis();
    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();


    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);
        crptApi.createDocument(getDoc(),"signature");
    }

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        if (requestLimit < 0) {
            requestLimit = 0;
        }
        this.requestLimit = requestLimit;
    }

    private final AtomicInteger requestCount = new AtomicInteger(0);

    public void createDocument(Doc doc,String signature) throws InterruptedException, IOException {

        Duration duration = timeUnit.toChronoUnit().getDuration();
        long time = duration.get(ChronoUnit.SECONDS);

        while (requestCount.get() >= requestLimit && System.currentTimeMillis() - start < time * 1000) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        if (System.currentTimeMillis() - start < time * 1000) {

            if (requestCount.get() < requestLimit) {
                new Thread(() -> {
                    requestCount.addAndGet(1);
                    try {
                        String requestBody = mapper.writeValueAsString(doc);

                        HttpClient httpClient = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build();

                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        requestCount.decrementAndGet();
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }



                }).start();
            }
        }
    }

    record Description(String participantInn) {
    }

    record Product(String certificate_document,
                   String certificate_document_date,
                   String certificate_document_number,
                   String owner_inn,
                   String producer_inn,
                   String production_date,
                   String tnved_code,
                   String uit_code,
                   String uitu_code) {

    }

    record Doc(Description description,
               String doc_id,
               String doc_status,
               String doc_type,
               boolean importRequest,
               String owner_inn,
               String participant_inn,
               String producer_inn,
               String production_date,
               String production_type,
               List<Product> products,
               String reg_date,
               String reg_number) {

    }

    private static CrptApi.Doc getDoc() {
        CrptApi.Description description = new CrptApi.Description("string");
        CrptApi.Product product = new CrptApi.Product("string",
                "2020-01-23",
                "string",
                "string",
                "string",
                "2020-01-23",
                "string",
                "string",
                "string");

        return new CrptApi.Doc(description,
                "string",
                "string",
                "string",
                true,
                "string",
                "string",
                "string",
                "2020-01-23",
                "string",
                List.of(product),
                "2020-01-23",
                "string");
    }


}

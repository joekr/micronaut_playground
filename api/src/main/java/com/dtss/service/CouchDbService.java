package com.dtss.service;


import io.micronaut.context.annotation.Value;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Controller;
import reactor.core.publisher.Mono;
import com.dtss.models.Event;

import javax.inject.Singleton;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@ExecuteOn(TaskExecutors.BLOCKING)
public class CouchDbService {

    @Inject
    @Client("${couchdb.host}")
    HttpClient httpClient;

    @Value("${couchdb.username}")
    String username;

    @Value("${couchdb.password}")
    String password;

    private static final String DATABASE_NAME = "event";

    // Create a database
    public HttpResponse<?> createDatabase(String dbName) {
        HttpRequest<?> request = HttpRequest.PUT("/" + dbName, "").basicAuth(username, password);
        return httpClient.toBlocking().exchange(request);
    }

    // Create a new document
    public Mono<HttpResponse<?>> createDocument(Map<String, Object> document) {

        String id = (String) document.get("_id");
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            document.put("_id", id);
        }

        document.remove("_rev");

        HttpRequest<?> request = HttpRequest.POST("/" + DATABASE_NAME, document).contentType(MediaType.APPLICATION_JSON).basicAuth(username, password);
        // return httpClient.toBlocking().exchange(request);
        return Mono.from(httpClient.exchange(request));
    }

    // Retrieve a document
    public Mono<HttpResponse<?>> getDocument(String dbName, String docId) {
        HttpRequest<?> request = HttpRequest.GET("/" + dbName + "/" + docId).basicAuth(username, password);
        return Mono.from(httpClient.exchange(request));
    }

    public Mono<HttpResponse<Object>> updateDocument(Event event) {
        // Convert the Event record to a Map
        Map<String, Object> documentMap = event.toMap();

        // Ensure that the _id and _rev are present in the map
        if (!documentMap.containsKey("_id") || !documentMap.containsKey("_rev")) {
            return Mono.error(new IllegalArgumentException("Document must have both _id and _rev for updates"));
        }

        // Send a PUT request to CouchDB to update the document
        HttpRequest<Map<String, Object>> request = HttpRequest.PUT("/" + DATABASE_NAME + "/" + event.id(), documentMap).basicAuth(username, password);
        
        return Mono.from(httpClient.exchange(request, Object.class))
            .doOnSuccess(response -> {
                System.out.println("Update successful: " + response.body());
            })
            .doOnError(error -> {
                System.err.println("Update failed: " + error.getMessage());
            });
    }

    public Mono<List<Event>> getUnprocessedEvents() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        // String currentTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);

        // Define the Mango query
        Map<String, Object> selector = Map.of(
                "selector", Map.of(
                        "processed", false,
                        "timestamp", Map.of("$lte", currentTime)
                )
        );

        HttpRequest<Map<String, Object>> request = HttpRequest.POST("/" + DATABASE_NAME + "/_find", selector).basicAuth(username, password);

        return Mono.from(httpClient.retrieve(request, Map.class))
            .map(resp -> {
                List<Map<String, Object>> docs = (List<Map<String, Object>>) resp.get("docs");
                return docs.stream()
                    .map(this::convertToEvent)
                    .collect(Collectors.toList());
            });
    }

    private Event convertToEvent(Map<String, Object> doc) {
        String id = (String) doc.get("_id");
        String rev = (String) doc.get("_rev");
        String name = (String) doc.get("name");
        String description = (String) doc.get("description");
        boolean processed = (Boolean) doc.get("processed");
        // LocalDateTime timestamp = (LocalDateTime) doc.get("timestamp");
        String timestampStr = (String) doc.get("timestamp");
        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);

        return new Event(id, name, timestamp, processed, description, rev);
    }
}
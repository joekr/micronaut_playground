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

import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;

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

    // Create a database
    public HttpResponse<?> createDatabase(String dbName) {
        HttpRequest<?> request = HttpRequest.PUT("/" + dbName, "").basicAuth(username, password);
        return httpClient.toBlocking().exchange(request);
    }

    // Save a document
    public Mono<HttpResponse<?>> saveDocument(String dbName, Map<String, Object> document) {

        String id = (String) document.get("_id");
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            document.put("_id", id);
        }

        HttpRequest<?> request = HttpRequest.POST("/" + dbName, document).contentType(MediaType.APPLICATION_JSON).basicAuth(username, password);
        // return httpClient.toBlocking().exchange(request);
        return Mono.from(httpClient.exchange(request));
    }

    // Retrieve a document
    public Mono<HttpResponse<?>> getDocument(String dbName, String docId) {
        HttpRequest<?> request = HttpRequest.GET("/" + dbName + "/" + docId).basicAuth(username, password);
        return Mono.from(httpClient.exchange(request));
    }
}
package com.dtss.service;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Controller;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class CouchDbService {

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
    public HttpResponse<?> saveDocument(String dbName, Map<String, Object> document) {
        HttpRequest<?> request = HttpRequest.POST("/" + dbName, document).contentType(MediaType.APPLICATION_JSON).basicAuth(username, password);
        return httpClient.toBlocking().exchange(request);
    }

    // Retrieve a document
    public HttpResponse<?> getDocument(String dbName, String docId) {
        HttpRequest<?> request = HttpRequest.GET("/" + dbName + "/" + docId).basicAuth(username, password);
        return httpClient.toBlocking().exchange(request);
    }
}

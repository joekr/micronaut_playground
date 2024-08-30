package com.dtss.controllers;

import java.util.Map;

import com.dtss.producers.EventProducer;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

@Controller("/event")
public class EventController {

    @Inject
    EventProducer eventProducer;
    
    @Get(produces = MediaType.APPLICATION_JSON)
    public Map<String, String> getApi(){
        return Map.of("message", "Hello, World!", "status", "success");
    }

    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, String>> createExample(@Body Map<String, String> input) {
        // Process the input data
        String name = input.getOrDefault("name", "Unknown");

        // Prepare the response data
        Map<String, String> response = Map.of("message", "Hello, " + name + "!", "status", "created");

        eventProducer.sendEvent(name);
        // Return a response with a custom status code
        return HttpResponse.created(response);
    }
}

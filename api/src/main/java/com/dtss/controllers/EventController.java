package com.dtss.controllers;

import java.util.HashMap;
import java.util.Map;

import com.dtss.producers.EventProducer;
import com.dtss.record.Event;
import com.dtss.service.CouchDbService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import javax.validation.Valid;
import reactor.core.publisher.Mono;

@Controller("/event")
public class EventController {

    @Inject
    EventProducer eventProducer;

    @Inject
    CouchDbService couchDbService;
    
    @Get(produces = MediaType.APPLICATION_JSON)
    public Map<String, String> getApi(){
        return Map.of("message", "Hello, World!", "status", "success");
    }

    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, String>> createExample(@Valid @Body Event event) {
        // Process the input data
        String name = event.name();

        // Prepare the response data
        Map<String, String> response = Map.of("message", "Hello, " + name + "!", "status", "created");


        try{
            Mono<HttpResponse<?>> dbResponse = couchDbService.saveDocument("event", event.toMap());
            dbResponse.subscribe(r-> {
                System.out.print("-----------------------");
                System.out.println(Mono.just(r.body()));
                System.out.print("-----------------------");
            });
        } catch (Exception e) {
            System.out.println(e);
        }

        eventProducer.sendEvent(name);
        // Return a response with a custom status code
        return HttpResponse.created(response);
    }
}

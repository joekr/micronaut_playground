package com.dtss.controllers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.dtss.producers.EventProducer;
import com.dtss.models.Event;
import com.dtss.service.CouchDbService;

import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import jakarta.inject.Inject;
import javax.validation.Valid;
import reactor.core.publisher.Mono;

@Controller("/event")
public class EventController {

    @Inject
    EventProducer eventProducer;

    @Inject
    CouchDbService couchDbService;

    @Inject
    ObjectMapper objectMapper;
    
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
                System.out.println(Mono.just(r.body()));
                // Extract the body from the HttpResponse
                ByteBuffer<?> byteBuffer = (ByteBuffer<?>) r.body();

                // Convert the ByteBuffer to a ByteBuf
                ByteBuf byteBuf = (ByteBuf) byteBuffer.asNativeBuffer();

                // Convert the NettyByteBuffer to a String
                String jsonString = byteBuffer.toString(StandardCharsets.UTF_8);
                System.out.println(jsonString);

                try {
                    Event dbEvent = objectMapper.readValue(jsonString, Event.class);
    
                    String eventId = dbEvent.id();
                    System.out.println("Event ID: " + eventId);
    
                    eventProducer.sendEvent(eventId);
                } catch (Exception e) {
                    System.err.println("Error deserializing response: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }

        // Return a response with a custom status code
        return HttpResponse.created(response);
    }
}

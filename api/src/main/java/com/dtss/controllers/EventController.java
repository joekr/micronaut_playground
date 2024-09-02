package com.dtss.controllers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.dtss.producers.EventProducer;
import com.dtss.annotation.RateLimited;
import com.dtss.interceptor.RateLimitingInterceptor;
import com.dtss.models.Event;
import com.dtss.service.CouchDbService;

import io.micronaut.aop.InterceptorBinding;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import jakarta.inject.Inject;
import javax.validation.Valid;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Controller("/event")
public class EventController {

    @Inject
    EventProducer eventProducer;

    @Inject
    CouchDbService couchDbService;

    @Inject
    ObjectMapper objectMapper;
    
    @RateLimited
    @Get(produces = MediaType.APPLICATION_JSON)
    public Map<String, String> getApi(){
        return Map.of("message", "Hello, World!", "status", "success");
    }

    @RateLimited(capacity = 100, refillTokens = 10, refillDurationSeconds = 10)
    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public Mono<MutableHttpResponse<Map<String, String>>> createExample(@Valid @Body Event event) {
        String name = event.name();

        Map<String, String> response = Map.of("created " + name + " event", "created");

        return couchDbService.saveDocument("event", event.toMap())
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
            .doBeforeRetry(retrySignal -> 
                System.out.println("Retrying due to: " + retrySignal.failure().getMessage()))
            ).map(r-> {
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
                } catch (JsonProcessingException e) {
                    System.err.println("Error deserializing response: " + e.getMessage());
                    return HttpResponse.<Map<String, String>>serverError(Map.of("error", "Failed to process event"));
                }

                return HttpResponse.created(response);
            }).onErrorReturn(HttpResponse.serverError(Map.of("error", "Operation failed after retries")));
    }
}

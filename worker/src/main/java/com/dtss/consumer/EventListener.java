package com.dtss.consumer;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtss.service.CouchDbService;
import io.micronaut.core.io.buffer.ByteBuffer;

@KafkaListener(groupId = "event-listener")
public class EventListener {

    @Inject
    CouchDbService couchDbService;

    private static final Logger LOG = LoggerFactory.getLogger(EventListener.class);

    @Topic("events")
    public void receive(String eventId) {
        LOG.info("Received eventId: {}", eventId);

        Mono<HttpResponse<?>> dbResponse = couchDbService.getDocument("event", eventId);
        dbResponse.subscribe(r-> {
            System.out.println(Mono.just(r.body()));
                // Extract the body from the HttpResponse
                ByteBuffer<?> byteBuffer = (ByteBuffer<?>) r.body();

                // Convert the ByteBuffer to a ByteBuf
                ByteBuf byteBuf = (ByteBuf) byteBuffer.asNativeBuffer();

                // Convert the NettyByteBuffer to a String
                String jsonString = byteBuffer.toString(StandardCharsets.UTF_8);
                System.out.println(jsonString);
        });

    }
}
package com.dtss.consumer;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(groupId = "event-listener")
public class EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventListener.class);

    @Topic("events")
    public void receive(String event) {
        LOG.info("Received event: {}", event);
    }
}
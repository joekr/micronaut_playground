package com.dtss.producers;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.MessageBody;

@KafkaClient
public interface EventProducer {

    @Topic("events")
    void sendEvent(@MessageBody String event);
}

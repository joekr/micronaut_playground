package com.dtss.job;

import java.util.List;
import java.util.Map;

import com.dtss.producers.EventProducer;
import com.dtss.service.CouchDbService;
import com.dtss.models.Event;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class EventJob{

    @Inject
    EventProducer eventProducer;

    @Inject
    CouchDbService couchDbService;

     @Scheduled(fixedDelay = "10s") // Runs every 10 seconds
    void processPendingEvents() {
        System.out.println(".... running processing job.");
        couchDbService.getUnprocessedEvents().subscribe(events -> {
            for (Event event : events) {
                String eventId = (String) event.id();
                System.out.println(".... running the event job "+ eventId);
                // Process each event
                eventProducer.sendEvent(eventId);
                
                // Update the document as processed
                Event updatedEvent = new Event(
                    event.id(), 
                    event.name(), 
                    event.timestamp(), 
                    true, // mark as processed
                    event.description(), 
                    event.revision()
                );

                couchDbService.updateDocument(updatedEvent).subscribe();
            }
        });
    }
}
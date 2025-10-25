package org.example.eventmanagermodule.Events;

import org.example.eventmanagermodule.producer.EventChangeNotification;
import org.example.eventmanagermodule.producer.EventProducerService;
import org.example.eventmanagermodule.producer.FieldChangeString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventStatusScheduler {
    private static final Logger log = LoggerFactory.getLogger(EventStatusScheduler.class);
    private final EventRepository eventRepository;
    private final EventProducerService eventProducerService;

    public EventStatusScheduler(EventRepository eventRepository, EventProducerService eventProducerService) {
        this.eventRepository = eventRepository;
        this.eventProducerService = eventProducerService;
    }

    @Scheduled(fixedRate = 360_000)
    public void updateEventStatuses() {
        List<EventEntity> events = eventRepository.findAll();
        List<EventChangeNotification> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        log.info("Scheduler started at {}", now);
        for (EventEntity event : events) {
            EventStatus oldStatus = event.getStatus();
            EventStatus newStatus = oldStatus;
            LocalDateTime endTime = event.getDate().plusMinutes(event.getDuration());
            if (oldStatus == EventStatus.CLOSED) {
                continue;
            }
            if (now.isBefore(event.getDate())) {
                newStatus = EventStatus.WAIT_START;
            } else if (now.isAfter(event.getDate()) && now.isBefore(endTime)) { // добавил event.getStatus()!= EventStatus.CLOSED
                newStatus = EventStatus.STARTED;
            } else if (now.isAfter(endTime)) {// добавил event.getStatus()!= EventStatus.CLOSED
                newStatus = EventStatus.FINISHED;
            }
            if (!oldStatus.equals(newStatus)) {
                event.setStatus(newStatus);
                FieldChangeString statusChange = new FieldChangeString(oldStatus.toString(), newStatus.toString());

                notifications.add(new EventChangeNotification(
                        event.getId(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        statusChange)
                );
            }
        }
        eventRepository.saveAll(events);
        notifications.forEach(eventProducerService::sendEventChange);
        log.info("Scheduler updated {} events", notifications.size());
    }
}
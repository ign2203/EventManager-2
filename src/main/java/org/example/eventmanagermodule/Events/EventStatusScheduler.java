package org.example.eventmanagermodule.Events;

import org.example.eventmanagermodule.eventmanager.EventProducerService;
import org.example.eventmanagermodule.eventmanager.status.EventStatusChangeNotification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventStatusScheduler {
    private final EventRepository eventRepository;
    private final EventProducerService eventProducerService;

    public EventStatusScheduler(EventRepository eventRepository, EventProducerService eventProducerService) {
        this.eventRepository = eventRepository;
        this.eventProducerService = eventProducerService;
    }

    @Scheduled(fixedRate = 360_000)
    public void updateEventStatuses() {
        List<EventEntity> events = eventRepository.findAll();
        List<EventStatusChangeNotification> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (EventEntity event : events) {
            EventStatus oldStatus = event.getStatus();
            EventStatus newStatus = oldStatus;
            LocalDateTime endTime = event.getDate().plusMinutes(event.getDuration());
            if (now.isBefore(event.getDate())&& event.getStatus()!= EventStatus.CLOSED) {
                newStatus = EventStatus.WAIT_START;
            } else if (now.isAfter(event.getDate()) && now.isBefore(endTime)) {
                newStatus = EventStatus.STARTED;
            } else if (now.isAfter(endTime)) {
                newStatus = EventStatus.FINISHED;
            }
            if (!oldStatus.equals(newStatus)) {
                event.setStatus(newStatus);
                notifications.add(new EventStatusChangeNotification(
                        event.getId(),
                        oldStatus,
                        newStatus
                ));
            }
        }
        eventRepository.saveAll(events);
        notifications.forEach(eventProducerService::sendStatusChangeNotification);
    }
}
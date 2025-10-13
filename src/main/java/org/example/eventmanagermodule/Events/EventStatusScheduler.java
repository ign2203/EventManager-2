package org.example.eventmanagermodule.Events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventStatusScheduler {
    private final EventRepository eventRepository;
    private final static Logger log = LoggerFactory.getLogger(EventService.class);

    public EventStatusScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void updateEventStatuses() {
        List<EventEntity> events = eventRepository.findAll();
        for (EventEntity event : events) {
            if (event.getStatus() != EventStatus.CLOSED) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endTime = event.getDate().plusMinutes(event.getDuration());
                if (now.isBefore(event.getDate())) {
                    event.setStatus(EventStatus.WAIT_START);
                } else if (now.isAfter(event.getDate()) && now.isBefore(endTime)) {
                    event.setStatus(EventStatus.STARTED);
                } else if (now.isAfter(endTime)) {
                    event.setStatus(EventStatus.FINISHED);
                }
            }
        }
        eventRepository.saveAll(events);
    }
}
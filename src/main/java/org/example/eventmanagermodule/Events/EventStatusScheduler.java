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
            LocalDateTime now = LocalDateTime.now(); // текущее время
            LocalDateTime endTime = event.getDate().plusMinutes(event.getDuration()); // время завершения мероприятия

            if (now.isBefore(event.getDate()) && now.isBefore(endTime)) {
                // если текущее время стало ДО isBefore времени начала и завершения мероприятия, можно сказать если  event.getDate())  <now<endTime, то STARTED
                event.setStatus(EventStatus.STARTED); // то статус началось
            } else if (now.isAfter(endTime)) {
                // если настоящее время  уже ПОСЛЕ endTime, то FINISHED, или если Now> endTime, то FINISHED
                event.setStatus(EventStatus.FINISHED);
            }

//            log.info("Event {} | start={} | end={} | now={} | status={}",
//                    event.getId(), event.getDate(), endTime, now, event.getStatus());
        }
        eventRepository.saveAll(events);
    }
}

// before - до
//after - после
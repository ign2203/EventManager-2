package dev.sorokin.eventnotificator.consumer.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.sorokin.eventnotificator.consumer.fieldChange.EventChangeNotification;
import dev.sorokin.eventnotificator.consumer.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "event-topic", groupId = "notificator-group")
    public void listenEventsKafka(ConsumerRecord<Long, EventChangeNotification> record) throws JsonProcessingException {
        EventChangeNotification event = record.value();
        if (event == null) {
            log.warn("[KAFKA] Received null event for key={}", record.key());
            return;
        }
        log.info("[KAFKA] Received event change notification for eventId={}", event.getEventId());
        notificationService.handleNotification(event);
    }
}
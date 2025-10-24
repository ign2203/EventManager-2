package org.example.eventmanagermodule.consumer.listener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.eventmanagermodule.consumer.NotificationService;
import org.example.eventmanagermodule.producer.EventChangeNotification;
import org.example.eventmanagermodule.producer.status.EventStatusChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
public class NotificationListener {
    private final static Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    public NotificationListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }
    @KafkaListener(topics = "event-topic", groupId = "notificator-group")
    public void listenEventsKafka(ConsumerRecord<Long, String> record) throws JsonProcessingException {
        logRecord(record);
        EventChangeNotification event = objectMapper.readValue(record.value(), EventChangeNotification.class);
        notificationService.handleNotification(event);
    }
    @KafkaListener(topics = "event-status-topic", groupId = "status-notificator-group")
    public void listenStatusChangeNotification (ConsumerRecord<Long, String> record) throws JsonProcessingException {
        logRecord(record);
        EventStatusChangeNotification notification = objectMapper.readValue(record.value(), EventStatusChangeNotification.class);
        notificationService.processEventStatusChange(notification);
    }
    private void logRecord(ConsumerRecord<Long, String> record) {
        if (record.value() == null) {
            log.warn("[KAFKA] Null value for key={}", record.key());
        } else {
            log.debug("[KAFKA] Key={}, Value={}", record.key(), record.value());
        }
    }
}

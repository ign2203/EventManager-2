package org.example.eventmanagermodule.producer;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class EventProducerService {
    private final static Logger log = LoggerFactory.getLogger(EventProducerService.class);
    private final KafkaTemplate<Long, EventChangeNotification> kafkaTemplate;

    public EventProducerService(KafkaTemplate<Long, EventChangeNotification> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEventChange(EventChangeNotification notification) {
        log.info(
                "Sending event change notification for eventId={} to topic='{}' with payload: {}",
                notification.eventId,
                "event-topic",
                notification
        );
        var result = kafkaTemplate.send(
                "event-topic",
                notification.eventId,
                notification
        );
        result.thenAccept(sendResult -> {
            log.info(
                    "Event change for eventId={} successfully sent to topic='{}', partition={}, offset={}",
                    notification.eventId,
                    "event-topic",
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset()
            );
        }
        );
    }
}

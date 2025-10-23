package org.example.eventmanagermodule.eventmanager;
import org.example.eventmanagermodule.eventmanager.status.EventStatusChangeNotification;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
@Service// у ментора компонент
public class EventProducerService {
    private final static Logger log = LoggerFactory.getLogger(EventProducerService.class);
    private final KafkaTemplate<Long, EventChangeNotification> kafkaTemplate;
    private final KafkaTemplate<Long, EventStatusChangeNotification> kafkaTemplateStatus;
    //Ключ (Long) обычно используют для партиционирования, значение (EventChangeNotification) — это то, что отправляем.
    public EventProducerService(KafkaTemplate<Long, EventChangeNotification> kafkaTemplate, KafkaTemplate<Long, EventStatusChangeNotification> kafkaTemplateStatus) {
        this.kafkaTemplate = kafkaTemplate;// не могу пояснить, но это самое событие в кафка, то есть это тип который может принимать кафка KafkaTemplate
        this.kafkaTemplateStatus = kafkaTemplateStatus;
    }
    public void sendEventChange(EventChangeNotification notification) {// метод по отправке события в кафку
        log.info("Sending new event={} change notification ...", notification);// логируем перед отправкой
        // KafkaTemplate состоит из трех частей: 1) Топик - не поясню 2) направляет сам id 3) сам объект нотификации, чтобы consumer мог с ним работать
        var result = kafkaTemplate.send( // ListenableFuture ругается, send ругается
                "event-topic", // имя топика, куда отправляем сообщение.
                notification.eventId,//ключ сообщения (Kafka использует ключ, чтобы определять партицию).
                notification//значение сообщения, то есть наш объект нотификации.
        );
        result.thenAccept(sendResult -> {//thenAccept — это обработчик успешного завершения асинхронной операции.thenAccept
            log.info("New event event update sent");
        });
    }
    public void sendStatusChangeNotification(EventStatusChangeNotification notification) {
        log.info("Sending new status change notification: {}", notification);
        var result = kafkaTemplateStatus.send(
                "event-status-topic",
                notification.eventId(),
                notification
        );
        result.thenAccept(sendResult -> {
            log.info("New status change event sent");
        });
    }
}

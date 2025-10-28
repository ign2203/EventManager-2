package org.example.eventmanagermodule.Location.web;

import org.example.eventmanagermodule.producer.EventChangeNotification;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;


@Configuration
public class KafkaConfig {
    @Bean
    public KafkaTemplate<Long, EventChangeNotification> kafkaTemplate(
            KafkaProperties kafkaProperties) {
        var props = kafkaProperties.buildProducerProperties(
                new DefaultSslBundleRegistry()
        );
        ProducerFactory<Long, EventChangeNotification> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }
}
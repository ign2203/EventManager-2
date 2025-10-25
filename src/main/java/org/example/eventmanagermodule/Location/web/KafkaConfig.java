package org.example.eventmanagermodule.Location.web;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.example.eventmanagermodule.producer.EventChangeNotification;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
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
    @Bean
    public ConsumerFactory<Long, EventChangeNotification> consumerFactory() {
        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "notificator-group");
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        var factory = new DefaultKafkaConsumerFactory<Long, EventChangeNotification>(configProperties);
        factory.setValueDeserializer(new JsonDeserializer<>(EventChangeNotification.class, false));
        return factory;
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, EventChangeNotification> containerFactory(
            ConsumerFactory<Long, EventChangeNotification> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, EventChangeNotification>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}

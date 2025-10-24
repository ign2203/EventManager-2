package org.example.eventmanagermodule.Location.web;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.example.eventmanagermodule.producer.EventChangeNotification;
import org.example.eventmanagermodule.producer.status.EventStatusChangeNotification;
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
    public KafkaTemplate<Long, EventChangeNotification> kafkaTemplate( // KafkaTemplate это главный инструмент для отправки сообщений в Kafka.
                                                                       KafkaProperties kafkaProperties) { //Это специальный объект Spring, который автоматически читает настройки Kafka из твоих application.properties или application.yml.
        var props = kafkaProperties.buildProducerProperties( //Это как список инструкций: куда отправлять, как сериализовать ключи и значения, таймауты и т.д.
                new DefaultSslBundleRegistry()// не понимаю
        );
        ProducerFactory<Long, EventChangeNotification> producerFactory = new DefaultKafkaProducerFactory<>(props); //«Создай фабрику, которая умеет собирать почтальонов (продюсеров) для Kafka.»
        return new KafkaTemplate<>(producerFactory);// «Создаём и возвращаем объект, который умеет отправлять события в Kafka.»
    }
    @Bean
    public ConsumerFactory<Long, EventChangeNotification> consumerFactory() { // это метод метод которые принимает нотификацию из кафки
        Map<String, Object> configProperties = new HashMap<>();// создаем болванку для хранения конфига
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");// кладем порт для подключения
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "notificator-group");// вот здесь я не понимаю
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);//здесь указываем что ключ у нас типа LONG
        var factory = new DefaultKafkaConsumerFactory<Long, EventChangeNotification>(configProperties);// далее создаем самих consumer-ов, где указываем ключ, саму нотификацию и подключение
        factory.setValueDeserializer(new JsonDeserializer<>(EventChangeNotification.class, false));// я так понимаю, этой строкой мы переводим из Json  в Java Объект
        return factory; // возвращаем consumer
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, EventChangeNotification> containerFactory(
            ConsumerFactory<Long, EventChangeNotification> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, EventChangeNotification>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
    @Bean
    public KafkaTemplate<Long, EventStatusChangeNotification> kafkaTemplateStatus (
                                                                       KafkaProperties kafkaProperties) {
        var props = kafkaProperties.buildProducerProperties(
                new DefaultSslBundleRegistry()
        );
        ProducerFactory<Long, EventStatusChangeNotification> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }
    @Bean
    public ConsumerFactory<Long, EventStatusChangeNotification> statusConsumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "status-notificator-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props,
                new LongDeserializer(),
                new JsonDeserializer<>(EventStatusChangeNotification.class, false));
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, EventStatusChangeNotification> statusContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, EventStatusChangeNotification>();
        factory.setConsumerFactory(statusConsumerFactory());
        return factory;
    }
}

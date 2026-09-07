package com.rally.inventory_service.config;

import com.rally.inventory_service.event.DealEvent;
import com.rally.inventory_service.event.ProductCreatedEvent;
import com.rally.inventory_service.event.ProductDeletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.listener.observation-enabled:false}")
    private boolean observationEnabled;

    private Map<String, Object> consumerProperties() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                autoOffsetReset
        );

        return properties;
    }

    private <T> JacksonJsonDeserializer<T> createDeserializer(Class<T> targetType) {
        JacksonJsonDeserializer<T> deserializer = new JacksonJsonDeserializer<>(targetType);
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        deserializer.setTypeMapper(typeMapper);
        return deserializer;
    }

    @Bean
    public ConsumerFactory<String, ProductCreatedEvent>
    productCreatedConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                createDeserializer(ProductCreatedEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent>
    productCreatedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(productCreatedConsumerFactory());
        factory.getContainerProperties().setObservationEnabled(observationEnabled);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, ProductDeletedEvent>
    productDeletedConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                createDeserializer(ProductDeletedEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductDeletedEvent>
    productDeletedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, ProductDeletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(productDeletedConsumerFactory());
        factory.getContainerProperties().setObservationEnabled(observationEnabled);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, DealEvent>
    dealEventConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                createDeserializer(DealEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DealEvent>
    dealEventKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, DealEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(dealEventConsumerFactory());
        factory.getContainerProperties().setObservationEnabled(observationEnabled);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, Object> orderLifecycleConsumerFactory() {
        Map<String, Object> props = consumerProperties();
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, HeaderTypeDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new HeaderTypeDeserializer())
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> orderLifecycleKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderLifecycleConsumerFactory());
        factory.getContainerProperties().setObservationEnabled(observationEnabled);
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                (record, exception) -> System.err.println("Skipping bad record: " + record + " — " + exception.getMessage())
        ));
        return factory;
    }
}

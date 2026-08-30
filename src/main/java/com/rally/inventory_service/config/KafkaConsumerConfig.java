package com.rally.inventory_service.config;

import com.rally.inventory_service.event.DealEvent;
import com.rally.inventory_service.event.ProductCreatedEvent;
import com.rally.inventory_service.event.ProductDeletedEvent;
import com.rally.inventory_service.event.OrderCompletedEvent;
import com.rally.inventory_service.event.OrderCancelledEvent;
import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.event.OrderNormalCancelledEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper;

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

        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderCompletedEvent>
    orderCompletedConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                createDeserializer(OrderCompletedEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCompletedEvent>
    orderCompletedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderCompletedConsumerFactory());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderCancelledEvent>
    orderCancelledConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                createDeserializer(OrderCancelledEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent>
    orderCancelledKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderCancelledConsumerFactory());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderNormalCancelledEvent>
    orderNormalCancelledConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                createDeserializer(OrderNormalCancelledEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderNormalCancelledEvent>
    orderNormalCancelledKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderNormalCancelledEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderNormalCancelledConsumerFactory());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent>
    orderCreatedConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                createDeserializer(OrderCreatedEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
    orderCreatedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderCreatedConsumerFactory());

        return factory;
    }
}

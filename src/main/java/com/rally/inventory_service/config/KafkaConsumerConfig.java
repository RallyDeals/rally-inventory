package com.rally.inventory_service.config;

import com.rally.inventory_service.event.DealCreatedEvent;
import com.rally.inventory_service.event.ProductCreatedEvent;
import com.rally.inventory_service.event.ProductDeletedEvent;
import com.rally.inventory_service.event.DealCancelledEvent;
import com.rally.inventory_service.event.DealExpiredEvent;
import com.rally.inventory_service.event.DealSucceededEvent;
import com.rally.inventory_service.event.OrderCompletedEvent;
import com.rally.inventory_service.event.OrderCancelledEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ConsumerFactory;
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

    @Bean
    public ConsumerFactory<String, ProductCreatedEvent>
    productCreatedConsumerFactory() {

        JacksonJsonDeserializer<ProductCreatedEvent> deserializer =
                new JacksonJsonDeserializer<>(ProductCreatedEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
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

        JacksonJsonDeserializer<ProductDeletedEvent> deserializer =
                new JacksonJsonDeserializer<>(ProductDeletedEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
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
    public ConsumerFactory<String, DealCreatedEvent>
    dealCreatedConsumerFactory() {

        JacksonJsonDeserializer<DealCreatedEvent> deserializer =
                new JacksonJsonDeserializer<>(DealCreatedEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DealCreatedEvent>
    dealCreatedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, DealCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(dealCreatedConsumerFactory());

        return factory;
    }
    @Bean
    public ConsumerFactory<String, DealCancelledEvent>
    dealCancelledConsumerFactory() {

        JacksonJsonDeserializer<DealCancelledEvent> deserializer =
                new JacksonJsonDeserializer<>(DealCancelledEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DealCancelledEvent>
    dealCancelledKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, DealCancelledEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(dealCancelledConsumerFactory());

        return factory;
    }
    @Bean
    public ConsumerFactory<String, DealExpiredEvent>
    dealExpiredConsumerFactory() {

        JacksonJsonDeserializer<DealExpiredEvent> deserializer =
                new JacksonJsonDeserializer<>(DealExpiredEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DealExpiredEvent>
    dealExpiredKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, DealExpiredEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(dealExpiredConsumerFactory());

        return factory;
    }
    @Bean
    public ConsumerFactory<String, DealSucceededEvent>
    dealSucceededConsumerFactory() {

        JacksonJsonDeserializer<DealSucceededEvent> deserializer =
                new JacksonJsonDeserializer<>(DealSucceededEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DealSucceededEvent>
    dealSucceededKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, DealSucceededEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(dealSucceededConsumerFactory());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderCompletedEvent>
    orderCompletedConsumerFactory() {

        JacksonJsonDeserializer<OrderCompletedEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderCompletedEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
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

        JacksonJsonDeserializer<OrderCancelledEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderCancelledEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                deserializer
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
}
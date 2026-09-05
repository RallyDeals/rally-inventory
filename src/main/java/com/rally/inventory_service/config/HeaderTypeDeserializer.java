package com.rally.inventory_service.config;

import com.rally.inventory_service.event.OrderCancelledEvent;
import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.listener.EventTypes;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HeaderTypeDeserializer implements Deserializer<Object> {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final String HEADER_EVENT_TYPE = "X-Type";

    @Override
    public Object deserialize(String topic, Headers headers, byte[] data) {
        if (data == null) return null;

        Header header = headers.lastHeader(HEADER_EVENT_TYPE);
        String type = header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;

        try {
            return switch (type) {
                case EventTypes.ORDER_CREATED -> mapper.readValue(data, OrderCreatedEvent.class);
                case EventTypes.ORDER_NORMAL_CANCELLED -> mapper.readValue(data, OrderCancelledEvent.class);
                case null, default -> throw new IllegalArgumentException("Unknown event type: " + type);
            };
        } catch (IllegalArgumentException e) {
            throw new SerializationException("Failed to deserialize event of type " + type, e);
        }
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        throw new UnsupportedOperationException("Use deserialize(topic, headers, data) instead");
    }
}
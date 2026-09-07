package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderCancelledEvent;
import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderLifecycleTopicListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderLifecycleTopicListener listener;

    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should deduct order stock on OrderCreatedEvent")
    void testOrderCreatedEvent() {
        OrderCreatedEvent.Item item = new OrderCreatedEvent.Item(productId, 3);
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(UUID.randomUUID());
        event.setItems(List.of(item));

        RecordHeaders headers = new RecordHeaders();
        headers.add("X-Type", "Order.Created".getBytes(StandardCharsets.UTF_8));

        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "order.lifecycle_events", 0, 0L, "key", event
        );
        record.headers().add("X-Type", "Order.Created".getBytes(StandardCharsets.UTF_8));

        listener.onMessage(record);

        verify(inventoryService).deductOrderStock(event.getItems());
    }

    @Test
    @DisplayName("Should release order stock on OrderCancelledEvent")
    void testOrderCancelledEvent() {
        OrderCancelledEvent.Item item = new OrderCancelledEvent.Item();
        item.setProductId(productId);
        item.setQuantity(2);

        OrderCancelledEvent event = new OrderCancelledEvent();
        event.setOrderId(UUID.randomUUID());
        event.setItems(List.of(item));

        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "order.lifecycle_events", 0, 0L, "key", event
        );
        record.headers().add("X-Type", "Order.Cancelled".getBytes(StandardCharsets.UTF_8));

        listener.onMessage(record);

        verify(inventoryService).releaseOrderStock(event.getItems());
    }

    @Test
    @DisplayName("Should do nothing on null record value or missing X-Type header")
    void testNullRecordOrMissingHeader() {
        ConsumerRecord<String, Object> nullRecord = new ConsumerRecord<>(
                "order.lifecycle_events", 0, 0L, "key", null
        );
        listener.onMessage(nullRecord);

        ConsumerRecord<String, Object> noHeaderRecord = new ConsumerRecord<>(
                "order.lifecycle_events", 0, 0L, "key", new OrderCreatedEvent()
        );
        listener.onMessage(noHeaderRecord);

        verifyNoInteractions(inventoryService);
    }
}

package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCreatedListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderCreatedListener orderCreatedListener;

    @Test
    @DisplayName("Should deduct order stock when order created with no cancel reason")
    void handleOrderCreatedValid() {
        OrderCreatedEvent.Item item = new OrderCreatedEvent.Item(UUID.randomUUID(), 3);
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(UUID.randomUUID());
        event.setItems(List.of(item));
        event.setCancelReason(null);

        orderCreatedListener.handleOrderCreated(event);

        verify(inventoryService).deductOrderStock(List.of(item));
    }

    @Test
    @DisplayName("Should ignore event if cancelReason is present on order.lifecycle topic")
    void handleOrderCreatedWithCancelReason() {
        OrderCreatedEvent.Item item = new OrderCreatedEvent.Item(UUID.randomUUID(), 3);
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(UUID.randomUUID());
        event.setItems(List.of(item));
        event.setCancelReason("PAYMENT_FAILED");

        orderCreatedListener.handleOrderCreated(event);

        verifyNoInteractions(inventoryService);
    }

    @Test
    @DisplayName("Should ignore null event or empty items")
    void handleOrderCreatedNullOrEmpty() {
        orderCreatedListener.handleOrderCreated(null);

        OrderCreatedEvent emptyEvent = new OrderCreatedEvent();
        emptyEvent.setItems(List.of());
        orderCreatedListener.handleOrderCreated(emptyEvent);

        verifyNoInteractions(inventoryService);
    }
}

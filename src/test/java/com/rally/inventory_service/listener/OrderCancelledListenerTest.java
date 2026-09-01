package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderCancelledEvent;
import com.rally.inventory_service.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCancelledListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderCancelledListener listener;

    @Test
    @DisplayName("Should release stock on OrderCancelledEvent")
    void handleOrderCancelled() {
        UUID productId = UUID.randomUUID();
        OrderCancelledEvent event = new OrderCancelledEvent(productId, 7);

        listener.handleOrderCancelled(event);

        verify(inventoryService).releaseStock(productId, 7);
    }
}

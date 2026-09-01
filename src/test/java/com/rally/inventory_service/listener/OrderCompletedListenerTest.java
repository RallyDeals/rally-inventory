package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderCompletedEvent;
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
class OrderCompletedListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderCompletedListener listener;

    @Test
    @DisplayName("Should deduct stock on OrderCompletedEvent")
    void handleOrderCompleted() {
        UUID productId = UUID.randomUUID();
        OrderCompletedEvent event = new OrderCompletedEvent(productId, 5);

        listener.handleOrderCompleted(event);

        verify(inventoryService).deductStock(productId, 5);
    }
}

package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderNormalCancelledEvent;
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
class OrderNormalCancelledListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderNormalCancelledListener listener;

    @Test
    @DisplayName("Should release order stock when cancelReason is present")
    void handleCancelledValid() {
        OrderNormalCancelledEvent.Item item = new OrderNormalCancelledEvent.Item();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(4);
        OrderNormalCancelledEvent event = new OrderNormalCancelledEvent();
        event.setOrderId(UUID.randomUUID());
        event.setItems(List.of(item));
        event.setCancelReason("USER_CANCELLED");

        listener.handleOrderNormalCancelled(event);

        verify(inventoryService).releaseOrderStock(List.of(item));
    }

    @Test
    @DisplayName("Should ignore event if cancelReason is null")
    void handleWithoutCancelReason() {
        OrderNormalCancelledEvent.Item item = new OrderNormalCancelledEvent.Item();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(4);
        OrderNormalCancelledEvent event = new OrderNormalCancelledEvent();
        event.setOrderId(UUID.randomUUID());
        event.setItems(List.of(item));
        event.setCancelReason(null);

        listener.handleOrderNormalCancelled(event);

        verifyNoInteractions(inventoryService);
    }

    @Test
    @DisplayName("Should ignore null event or empty items")
    void handleNullOrEmpty() {
        listener.handleOrderNormalCancelled(null);

        OrderNormalCancelledEvent emptyEvent = new OrderNormalCancelledEvent();
        emptyEvent.setItems(List.of());
        emptyEvent.setCancelReason("CANCELLED");
        listener.handleOrderNormalCancelled(emptyEvent);

        verifyNoInteractions(inventoryService);
    }
}

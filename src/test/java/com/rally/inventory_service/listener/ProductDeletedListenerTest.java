package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.ProductDeletedEvent;
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
class ProductDeletedListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductDeletedListener listener;

    @Test
    @DisplayName("Should delete inventory on ProductDeletedEvent")
    void handleProductDeleted() {
        UUID productId = UUID.randomUUID();
        ProductDeletedEvent event = new ProductDeletedEvent(productId);

        listener.handleProductDeleted(event);

        verify(inventoryService).deleteInventory(productId);
    }
}

package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.ProductCreatedEvent;
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
class ProductCreatedListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductCreatedListener listener;

    @Test
    @DisplayName("Should create inventory on ProductCreatedEvent")
    void handleProductCreated() {
        UUID productId = UUID.randomUUID();
        ProductCreatedEvent event = new ProductCreatedEvent(productId, 100);

        listener.handleProductCreated(event);

        verify(inventoryService).createInventory(productId, 100);
    }
}

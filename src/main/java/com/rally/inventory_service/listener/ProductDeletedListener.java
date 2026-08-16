package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.ProductDeletedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductDeletedListener {

    private final InventoryService inventoryService;

    public ProductDeletedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "product-deleted",
            containerFactory = "productDeletedKafkaListenerContainerFactory"
    )
    public void handleProductDeleted(ProductDeletedEvent event) {

        inventoryService.deleteInventory(event.getProductId());
    }
}

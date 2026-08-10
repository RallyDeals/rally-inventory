package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.ProductCreatedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductCreatedListener {

    private final InventoryService inventoryService;

    public ProductCreatedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "product-created",
            containerFactory = "productCreatedKafkaListenerContainerFactory"
    )
    public void handleProductCreated(ProductCreatedEvent event) {

        inventoryService.createInventory(
                event.getProductId(),
                event.getInitialStock()
        );
    }
}


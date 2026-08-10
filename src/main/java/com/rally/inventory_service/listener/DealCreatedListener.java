package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.DealCreatedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DealCreatedListener {

    private final InventoryService inventoryService;

    public DealCreatedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "deal-created",
            containerFactory = "dealCreatedKafkaListenerContainerFactory"
    )
    public void handleDealCreated(DealCreatedEvent event) {

        inventoryService.reserveStock(
                event.getProductId(),
                event.getQuantity()
        );
    }
}
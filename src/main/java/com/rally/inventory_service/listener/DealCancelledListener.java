package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.DealCancelledEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DealCancelledListener {

    private final InventoryService inventoryService;

    public DealCancelledListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "deal-cancelled",
            groupId = "inventory-service",
            containerFactory = "dealCancelledKafkaListenerContainerFactory"
    )
    public void handleDealCancelled(DealCancelledEvent event) {

        inventoryService.releaseStock(
                event.getProductId(),
                event.getQuantity()
        );
    }
}
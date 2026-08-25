package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.DealFailedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DealFailedListener {

    private final InventoryService inventoryService;

    public DealFailedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "deal-failed",
            groupId = "inventory-service",
            containerFactory = "dealFailedKafkaListenerContainerFactory"
    )
    public void handleDealFailed(DealFailedEvent event) {

        inventoryService.releaseStock(
                event.getProductId(),
                event.getQuantity()
        );
    }
}
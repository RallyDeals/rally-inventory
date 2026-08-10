package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.DealExpiredEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DealExpiredListener {

    private final InventoryService inventoryService;

    public DealExpiredListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "deal-expired",
            groupId = "inventory-service",
            containerFactory = "dealExpiredKafkaListenerContainerFactory"
    )
    public void handleDealExpired(DealExpiredEvent event) {

        inventoryService.releaseStock(
                event.getProductId(),
                event.getQuantity()
        );
    }
}
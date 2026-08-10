package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.DealSucceededEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DealSucceededListener {

    private final InventoryService inventoryService;

    public DealSucceededListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "deal-succeeded",
            groupId = "inventory-service",
            containerFactory = "dealSucceededKafkaListenerContainerFactory"
    )
    public void handleDealSucceeded(DealSucceededEvent event) {

        inventoryService.deductStock(
                event.getProductId(),
                event.getQuantity()
        );
    }
}
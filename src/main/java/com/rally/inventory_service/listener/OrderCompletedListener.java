package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderCompletedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCompletedListener {

    private final InventoryService inventoryService;

    public OrderCompletedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "order.lifecycle_events",
            groupId = "inventory-service",
            containerFactory = "orderCompletedKafkaListenerContainerFactory"
    )
    public void handleOrderCompleted(OrderCompletedEvent event) {

        inventoryService.deductStock(
                event.getProductId(),
                event.getQuantity()
        );
    }
}
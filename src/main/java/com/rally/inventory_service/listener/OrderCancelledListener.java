package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderCancelledEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelledListener {

    private final InventoryService inventoryService;

    public OrderCancelledListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "order.lifecycle_events",
            groupId = "inventory-service",
            containerFactory = "orderCancelledKafkaListenerContainerFactory"
    )
    public void handleOrderCancelled(OrderCancelledEvent event) {

        inventoryService.releaseStock(
                event.getProductId(),
                event.getQuantity()
        );
    }
}
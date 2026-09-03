package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderNormalCancelledEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNormalCancelledListener {

    private final InventoryService inventoryService;

    public OrderNormalCancelledListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "order.lifecycle_events",
            groupId = "inventory-service",
            containerFactory = "orderNormalCancelledKafkaListenerContainerFactory"
    )
    public void handleOrderNormalCancelled(OrderNormalCancelledEvent event) {

        if (event == null || event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        // On order.lifecycle topic, only process if this is a cancellation event
        if (event.getCancelReason() == null) {
            return;
        }

        inventoryService.releaseOrderStock(event.getItems());
    }
}


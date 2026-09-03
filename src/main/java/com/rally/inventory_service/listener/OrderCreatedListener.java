package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private final InventoryService inventoryService;

    public OrderCreatedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "order.lifecycle_events",
            groupId = "inventory-service",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {

        if (event == null || event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        // On order.lifecycle topic, ignore if this is a cancellation event
        if (event.getCancelReason() != null) {
            return;
        }

        inventoryService.deductOrderStock(event.getItems());
    }
}

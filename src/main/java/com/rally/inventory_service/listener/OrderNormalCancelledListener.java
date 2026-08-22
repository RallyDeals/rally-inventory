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
            topics = "Order.NormalCancelled",
            groupId = "inventory-service",
            containerFactory = "orderNormalCancelledKafkaListenerContainerFactory"
    )
    public void handleOrderNormalCancelled(OrderNormalCancelledEvent event) {

        inventoryService.releaseOrderStock(event.getItems());
    }
}

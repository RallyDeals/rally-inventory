package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.DealEvent;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class DealEventListener {

    private final InventoryService inventoryService;

    public DealEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "deal",
            groupId = "inventory-service",
            containerFactory = "dealEventKafkaListenerContainerFactory"
    )
    public void handleDealEvent(
            DealEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String dealId,
            @Header("X-Type") String eventType
    ) {
        switch (eventType) {
            case "Deal.Created" -> inventoryService.reserveStock(
                    event.getProductId(),
                    event.getQuantity()
            );
            case "Deal.Cancelled" -> inventoryService.releaseStock(
                    event.getProductId(),
                    event.getQuantity()
            );
            case "Deal.Failed" -> inventoryService.releaseStock(
                    event.getProductId(),
                    event.getQuantity()
            );
            case "Deal.Succeeded" -> inventoryService.releaseStock(
                    event.getProductId(),
                    event.getQuantity()
            );
            case "Deal.Activated" -> {
                // No inventory action needed - already reserved at deal creation
            }
            default -> {
                // Unknown event type - log or ignore
            }
        }
    }
}
package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.OrderCancelledEvent;
import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.service.InventoryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OrderLifecycleTopicListener {

    private final InventoryService inventoryService;
    private static final String HEADER_EVENT_TYPE = "X-Type";
    public OrderLifecycleTopicListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }


    @KafkaListener(
            topics = "order.lifecycle_events",
            groupId = "inventory-service",
            containerFactory = "orderLifecycleKafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, Object> record){
        if (record.value() == null) return;
        String eventType = extractType(record);
        if (eventType == null) {
            return;
        }
        switch(record.value()){
            case OrderCreatedEvent e -> inventoryService.deductOrderStock(e.getItems());
            case OrderCancelledEvent e -> inventoryService.releaseOrderStock(e.getItems());
            default -> System.out.println("Unhandled event type: " + eventType);
        }
    }
    private String extractType(ConsumerRecord<String, Object> record) {
        Header header = record.headers().lastHeader(HEADER_EVENT_TYPE);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}
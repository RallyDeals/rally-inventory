package com.rally.inventory_service.event;

import java.util.UUID;

public class DealCreatedEvent {

    private UUID productId;
    private Integer quantity;

    public DealCreatedEvent() {
    }

    public DealCreatedEvent(UUID productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
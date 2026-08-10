package com.rally.inventory_service.event;

import java.util.UUID;

public class OrderCancelledEvent {

    private UUID productId;
    private Integer quantity;

    public OrderCancelledEvent() {
    }

    public OrderCancelledEvent(UUID productId, Integer quantity) {
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
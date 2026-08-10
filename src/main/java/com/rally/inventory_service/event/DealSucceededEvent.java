package com.rally.inventory_service.event;

import java.util.UUID;

public class DealSucceededEvent {

    private UUID productId;
    private Integer quantity;

    public DealSucceededEvent() {
    }

    public DealSucceededEvent(UUID productId, Integer quantity) {
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
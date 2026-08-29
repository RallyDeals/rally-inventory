package com.rally.inventory_service.event;

import java.util.UUID;

public class DealFailedEvent {

    private UUID productId;
    private Integer quantity;

    public DealFailedEvent() {
    }

    public DealFailedEvent(UUID productId, Integer quantity) {
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

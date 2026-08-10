package com.rally.inventory_service.event;

import java.util.UUID;

public class ProductCreatedEvent {

    private UUID productId;
    private Integer initialStock;

    public ProductCreatedEvent() {
    }

    public ProductCreatedEvent(UUID productId, Integer initialStock) {
        this.productId = productId;
        this.initialStock = initialStock;
    }

    public UUID getProductId() {
        return productId;
    }

    public Integer getInitialStock() {
        return initialStock;
    }
}
package com.rally.inventory_service.event;

import java.util.UUID;

public class ProductDeletedEvent {

    private UUID productId;

    public ProductDeletedEvent() {
    }

    public ProductDeletedEvent(UUID productId) {
        this.productId = productId;
    }

    public UUID getProductId() {
        return productId;
    }
}

package com.rally.inventory_service.event;

import java.util.UUID;

public class DealEvent {

    private UUID productId;
    private Integer quantity;
    private Integer reservedStock;
    private Integer authorizedCount;

    public DealEvent() {
    }

    public DealEvent(UUID productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(Integer reservedStock) {
        this.reservedStock = reservedStock;
    }

    public Integer getAuthorizedCount() {
        return authorizedCount;
    }

    public void setAuthorizedCount(Integer authorizedCount) {
        this.authorizedCount = authorizedCount;
    }
}
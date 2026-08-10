package com.rally.inventory_service.dto;

public class ReserveInventoryRequest {

    private Integer quantity;

    public ReserveInventoryRequest() {
    }

    public ReserveInventoryRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
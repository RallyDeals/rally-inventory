package com.rally.inventory_service.dto;

import java.util.List;
import java.util.UUID;

public class OrderReserveResponse {

    private UUID orderId;
    private List<Item> items;

    public OrderReserveResponse() {
    }

    public OrderReserveResponse(UUID orderId, List<Item> items) {
        this.orderId = orderId;
        this.items = items;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {

        private UUID productId;
        private Integer available;
        private Boolean reserved;

        public Item() {
        }

        public Item(UUID productId, Integer available, Boolean reserved) {
            this.productId = productId;
            this.available = available;
            this.reserved = reserved;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public Integer getAvailable() {
            return available;
        }

        public void setAvailable(Integer available) {
            this.available = available;
        }

        public Boolean getReserved() {
            return reserved;
        }

        public void setReserved(Boolean reserved) {
            this.reserved = reserved;
        }
    }
}

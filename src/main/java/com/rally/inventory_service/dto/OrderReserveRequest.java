package com.rally.inventory_service.dto;

import java.util.List;
import java.util.UUID;

public class OrderReserveRequest {

    private UUID orderId;
    private List<Item> items;

    public OrderReserveRequest() {
    }

    public OrderReserveRequest(UUID orderId, List<Item> items) {
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
        private Integer quantity;

        public Item() {
        }

        public Item(UUID productId, Integer quantity) {
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
    }
}

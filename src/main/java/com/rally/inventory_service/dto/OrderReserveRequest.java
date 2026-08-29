package com.rally.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderReserveRequest {

    @JsonProperty("orderId")
    @JsonAlias({"orderId", "order_id"})
    private UUID orderId;

    @JsonProperty("items")
    @JsonAlias("items")
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @JsonProperty("productId")
        @JsonAlias({"productId", "product_id"})
        private UUID productId;

        @JsonProperty("quantity")
        @JsonAlias("quantity")
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


package com.rally.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderReserveResponse {

    @JsonProperty("orderId")
    @JsonAlias({"orderId", "order_id"})
    private UUID orderId;

    @JsonProperty("items")
    @JsonAlias("items")
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @JsonProperty("productId")
        @JsonAlias({"productId", "product_id"})
        private UUID productId;

        @JsonProperty("available")
        @JsonAlias("available")
        private Integer available;

        @JsonProperty("reserved")
        @JsonAlias("reserved")
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


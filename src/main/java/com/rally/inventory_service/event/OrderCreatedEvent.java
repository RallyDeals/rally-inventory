package com.rally.inventory_service.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreatedEvent {

    @JsonProperty("order_id")
    @JsonAlias({"order_id", "orderId"})
    private UUID orderId;

    @JsonProperty("user_id")
    @JsonAlias({"user_id", "userId"})
    private UUID userId;

    @JsonProperty("items")
    @JsonAlias("items")
    private List<Item> items;

    @JsonProperty("total_price")
    @JsonAlias({"total_price", "totalPrice"})
    private BigDecimal totalPrice;

    @JsonProperty("address")
    @JsonAlias("address")
    private String address;

    @JsonProperty("cancelReason")
    @JsonAlias({"cancelReason", "cancel_reason"})
    private String cancelReason;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(UUID orderId, UUID userId, List<Item> items, BigDecimal totalPrice, String address) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.address = address;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @JsonProperty("product_id")
        @JsonAlias({"product_id", "productId"})
        private UUID productId;

        @JsonProperty("product_name")
        @JsonAlias({"product_name", "productName"})
        private String productName;

        @JsonProperty("product_image_url")
        @JsonAlias({"product_image_url", "productImageUrl"})
        private String productImageUrl;

        @JsonProperty("quantity")
        @JsonAlias("quantity")
        private Integer quantity;

        @JsonProperty("unit_price")
        @JsonAlias({"unit_price", "unitPrice"})
        private BigDecimal unitPrice;

        public Item() {
        }

        public Item(UUID productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Item(UUID productId, String productName, String productImageUrl, Integer quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.productImageUrl = productImageUrl;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductImageUrl() {
            return productImageUrl;
        }

        public void setProductImageUrl(String productImageUrl) {
            this.productImageUrl = productImageUrl;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}

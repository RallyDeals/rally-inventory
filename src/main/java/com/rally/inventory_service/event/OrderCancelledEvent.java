package com.rally.inventory_service.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCancelledEvent {

    @JsonProperty("order_id")
    @JsonAlias({"order_id", "orderId"})
    private UUID orderId;

    @JsonProperty("user_id")
    @JsonAlias({"user_id", "userId"})
    private UUID userId;

    @JsonProperty("cancelReason")
    @JsonAlias({"cancelReason", "cancel_reason"})
    private String cancelReason;

    @JsonProperty("items")
    @JsonAlias("items")
    private List<Item> items;

    @JsonProperty("total_price")
    @JsonAlias({"total_price", "totalPrice"})
    private BigDecimal totalPrice;

    @JsonProperty("payment_error_message")
    @JsonAlias({"payment_error_message", "paymentErrorMessage"})
    private String paymentErrorMessage;

    public OrderCancelledEvent() {
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

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
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

    public String getPaymentErrorMessage() {
        return paymentErrorMessage;
    }

    public void setPaymentErrorMessage(String paymentErrorMessage) {
        this.paymentErrorMessage = paymentErrorMessage;
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


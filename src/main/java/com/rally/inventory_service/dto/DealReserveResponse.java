package com.rally.inventory_service.dto;

/**
 * Structured JSON response for POST /inventory/{productId}/reserve-deal
 * and POST /inventory/{productId}/reserve-order.
 *
 * Always returns HTTP 200 so the caller can deserialize the body
 * and inspect {@code success} rather than catching HTTP errors.
 */
public class DealReserveResponse {

    private boolean success;
    private String reason;          // null when success=true
    private Integer availableStock; // actual available stock at the time of the call

    public DealReserveResponse() {}

    public DealReserveResponse(boolean success, String reason, Integer availableStock) {
        this.success = success;
        this.reason = reason;
        this.availableStock = availableStock;
    }

    // Static factories ---------------------------------------------------------

    public static DealReserveResponse ok() {
        return new DealReserveResponse(true, null, null);
    }

    public static DealReserveResponse insufficientStock(int availableStock) {
        return new DealReserveResponse(false, "INSUFFICIENT_STOCK", availableStock);
    }

    public static DealReserveResponse notFound() {
        return new DealReserveResponse(false, "PRODUCT_NOT_FOUND", null);
    }

    // Getters / setters --------------------------------------------------------

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
}

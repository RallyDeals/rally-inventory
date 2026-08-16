package com.rally.inventory_service.dto;

public class AdjustRequest {

    private Integer adjustment;
    private String reason;

    public AdjustRequest() {
    }

    public AdjustRequest(Integer adjustment, String reason) {
        this.adjustment = adjustment;
        this.reason = reason;
    }

    public Integer getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(Integer adjustment) {
        this.adjustment = adjustment;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

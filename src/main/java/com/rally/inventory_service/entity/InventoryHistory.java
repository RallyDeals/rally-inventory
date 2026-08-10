package com.rally.inventory_service.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_history")
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private InventoryOperationType operationType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public InventoryHistory() {
    }

    public InventoryHistory(
            UUID productId,
            InventoryOperationType operationType,
            Integer quantity,
            OffsetDateTime createdAt
    ) {
        this.productId = productId;
        this.operationType = operationType;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public InventoryOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(InventoryOperationType operationType) {
        this.operationType = operationType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
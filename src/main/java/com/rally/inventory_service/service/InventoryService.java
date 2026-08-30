package com.rally.inventory_service.service;

import com.rally.inventory_service.dto.DealReserveResponse;
import com.rally.inventory_service.dto.OrderReserveRequest;
import com.rally.inventory_service.dto.OrderReserveResponse;
import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.event.OrderNormalCancelledEvent;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    void createInventory(UUID productId, Integer initialStock);
    DealReserveResponse reserveStock(UUID productId, Integer quantity);
    OrderReserveResponse reserveOrder(OrderReserveRequest request);
    void releaseStock(UUID productId, Integer quantity);
    void releaseOrderStock(List<OrderNormalCancelledEvent.Item> items);
    void deductStock(UUID productId, Integer quantity);
    void deductOrderStock(List<OrderCreatedEvent.Item> items);
    void restock(UUID productId, Integer quantity);
    void adjustInventory(UUID productId, Integer adjustment);
    void deleteInventory(UUID productId);
    Inventory getInventory(UUID productId);
    List<Inventory> getInventoryBulk(List<UUID> productIds);
}


package com.rally.inventory_service.service;

import com.rally.inventory_service.entity.Inventory;
import java.util.UUID;

public interface InventoryService {

    void createInventory(UUID productId, Integer initialStock);
    void reserveStock(UUID productId, Integer quantity);
    void releaseStock(UUID productId, Integer quantity);
    void deductStock(UUID productId, Integer quantity);
    void restock(UUID productId, Integer quantity);
    Inventory getInventory(UUID productId);
}
package com.rally.inventory_service.service.impl;

import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.entity.InventoryHistory;
import com.rally.inventory_service.entity.InventoryOperationType;
import com.rally.inventory_service.repository.InventoryHistoryRepository;
import com.rally.inventory_service.repository.InventoryRepository;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository historyRepository;
    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryHistoryRepository historyRepository) {

        this.inventoryRepository = inventoryRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void createInventory(UUID productId, Integer initialStock) {

        // Check if inventory already exists
        if (inventoryRepository.existsById(productId)) {
            throw new IllegalArgumentException(
                    "Inventory already exists for product: " + productId
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        // Create inventory
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setTotalStock(initialStock);
        inventory.setReservedStock(0);
        inventory.setAvailableStock(initialStock);
        inventory.setUpdatedAt(now);

        inventoryRepository.save(inventory);

        // Create history record
        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setOperationType(InventoryOperationType.CREATE);
        history.setQuantity(initialStock);
        history.setCreatedAt(now);

        historyRepository.save(history);
    }


    @Override
    public void reserveStock(UUID productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for product: " + productId
                        ));

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Reservation quantity must be greater than zero"
            );
        }

        if (inventory.getAvailableStock() < quantity) {
            throw new IllegalStateException(
                    "Insufficient available stock for product: " + productId
            );
        }

        inventory.setReservedStock(
                inventory.getReservedStock() + quantity
        );

        inventory.setAvailableStock(
                inventory.getAvailableStock() - quantity
        );

        OffsetDateTime now = OffsetDateTime.now();
        inventory.setUpdatedAt(now);

        inventoryRepository.save(inventory);

        // Create history record
        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setOperationType(InventoryOperationType.RESERVE);
        history.setQuantity(quantity);
        history.setCreatedAt(now);

        historyRepository.save(history);
    }

    @Override
    public void releaseStock(UUID productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for product: " + productId
                        ));

        if (inventory.getReservedStock() < quantity) {
            throw new IllegalStateException(
                    "Cannot release more stock than currently reserved for product: "
                            + productId
            );
        }

        inventory.setReservedStock(
                inventory.getReservedStock() - quantity
        );

        inventory.setAvailableStock(
                inventory.getAvailableStock() + quantity
        );

        inventory.setUpdatedAt(OffsetDateTime.now());

        inventoryRepository.save(inventory);
        // Record release operation
        OffsetDateTime now = OffsetDateTime.now();
        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setOperationType(InventoryOperationType.RELEASE);
        history.setQuantity(quantity);
        history.setCreatedAt(now);

        historyRepository.save(history);
    }

    @Override
    public void deductStock(UUID productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for product: " + productId
                        ));

        if (inventory.getReservedStock() < quantity) {
            throw new IllegalStateException(
                    "Cannot deduct more stock than currently reserved for product: "
                            + productId
            );
        }

        inventory.setTotalStock(
                inventory.getTotalStock() - quantity
        );

        inventory.setReservedStock(
                inventory.getReservedStock() - quantity
        );

        inventory.setUpdatedAt(OffsetDateTime.now());

        inventoryRepository.save(inventory);
        // Create history record
        OffsetDateTime now = OffsetDateTime.now();
        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setOperationType(InventoryOperationType.DEDUCT);
        history.setQuantity(quantity);
        history.setCreatedAt(now);

        historyRepository.save(history);
    }

    @Override
    public void restock(UUID productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for product: " + productId
                        ));

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Restock quantity must be greater than zero"
            );
        }

        inventory.setTotalStock(
                inventory.getTotalStock() + quantity
        );

        inventory.setAvailableStock(
                inventory.getAvailableStock() + quantity
        );
        OffsetDateTime now = OffsetDateTime.now();

        inventory.setUpdatedAt(now);

        inventoryRepository.save(inventory);
        // Create history record
        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setOperationType(InventoryOperationType.RESTOCK);
        history.setQuantity(quantity);
        history.setCreatedAt(now);

        historyRepository.save(history);
    }

    @Override
    public Inventory getInventory(UUID productId) {

        return inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for product: " + productId
                        ));
    }

    @Override
    public void adjustInventory(UUID productId, Integer adjustment) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for product: " + productId
                        ));

        int newTotal = inventory.getTotalStock() + adjustment;
        if (newTotal < 0) {
            throw new IllegalArgumentException(
                    "Adjustment would result in negative stock for product: " + productId
            );
        }

        inventory.setTotalStock(newTotal);
        inventory.setAvailableStock(newTotal - inventory.getReservedStock());

        OffsetDateTime now = OffsetDateTime.now();
        inventory.setUpdatedAt(now);

        inventoryRepository.save(inventory);

        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setOperationType(InventoryOperationType.UPDATE);
        history.setQuantity(Math.abs(adjustment));
        history.setCreatedAt(now);

        historyRepository.save(history);
    }

    @Override
    public void deleteInventory(UUID productId) {

        if (!inventoryRepository.existsById(productId)) {
            return;
        }

        inventoryRepository.deleteById(productId);
    }

}
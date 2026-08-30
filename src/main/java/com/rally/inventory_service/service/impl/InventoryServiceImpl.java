package com.rally.inventory_service.service.impl;

import com.rally.common.exceptions.domain.inventory.InsufficientStockException;
import com.rally.common.exceptions.domain.inventory.InventoryNotFoundException;
import com.rally.common.exceptions.shared.AlreadyExistsException;
import com.rally.common.exceptions.shared.BadRequestException;
import com.rally.common.exceptions.shared.ConflictException;
import com.rally.inventory_service.dto.DealReserveResponse;
import com.rally.inventory_service.dto.OrderReserveRequest;
import com.rally.inventory_service.dto.OrderReserveResponse;
import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.entity.InventoryHistory;
import com.rally.inventory_service.entity.InventoryOperationType;
import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.event.OrderNormalCancelledEvent;
import com.rally.inventory_service.repository.InventoryHistoryRepository;
import com.rally.inventory_service.repository.InventoryRepository;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        if (productId == null) {
            throw new BadRequestException("Product id is required");
        }

        if (initialStock == null || initialStock < 0) {
            throw new BadRequestException("Initial stock must be zero or greater");
        }

        if (inventoryRepository.existsById(productId)) {
            throw new AlreadyExistsException(
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
    public DealReserveResponse reserveStock(UUID productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findById(productId).orElse(null);
        if (inventory == null) {
            return DealReserveResponse.notFound();
        }

        if (quantity <= 0) {
            return new DealReserveResponse(false, "INVALID_QUANTITY", inventory.getAvailableStock());
        }

        if (inventory.getAvailableStock() < quantity) {
            return DealReserveResponse.insufficientStock(inventory.getAvailableStock());
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

        return DealReserveResponse.ok();
    }

    @Override
    public OrderReserveResponse reserveOrder(OrderReserveRequest request) {

        if (request == null || request.getOrderId() == null) {
            throw new BadRequestException("Order id is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("At least one order item is required");
        }

        for (OrderReserveRequest.Item item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new BadRequestException("Product id is required");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException(
                        "Reservation quantity must be greater than zero for product: "
                                + item.getProductId()
                );
            }
        }

        Map<UUID, Integer> totalRequestedPerProduct = new LinkedHashMap<>();
        for (OrderReserveRequest.Item item : request.getItems()) {
            totalRequestedPerProduct.merge(
                    item.getProductId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }

        Map<UUID, Inventory> inventories = new LinkedHashMap<>();
        Map<UUID, Integer> availableStockMap = new LinkedHashMap<>();
        boolean allAvailable = true;

        for (Map.Entry<UUID, Integer> entry : totalRequestedPerProduct.entrySet()) {
            UUID productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            Optional<Inventory> inventoryOpt = inventoryRepository.findById(productId);
            if (inventoryOpt.isEmpty()) {
                allAvailable = false;
                availableStockMap.put(productId, 0);
            } else {
                Inventory inventory = inventoryOpt.get();
                inventories.put(productId, inventory);
                availableStockMap.put(productId, inventory.getAvailableStock());
                if (inventory.getAvailableStock() < requestedQuantity) {
                    allAvailable = false;
                }
            }
        }

        List<OrderReserveResponse.Item> responseItems = new ArrayList<>();
        for (OrderReserveRequest.Item item : request.getItems()) {
            UUID productId = item.getProductId();
            int available = availableStockMap.getOrDefault(productId, 0);
            int totalRequested = totalRequestedPerProduct.getOrDefault(productId, item.getQuantity());
            boolean itemReserved = (inventories.containsKey(productId) && available >= totalRequested);

            responseItems.add(
                    new OrderReserveResponse.Item(
                            productId,
                            available,
                            itemReserved
                    )
            );
        }

        if (allAvailable) {
            OffsetDateTime now = OffsetDateTime.now();

            for (Map.Entry<UUID, Integer> entry : totalRequestedPerProduct.entrySet()) {
                UUID productId = entry.getKey();
                Integer quantity = entry.getValue();
                Inventory inventory = inventories.get(productId);

                inventory.setReservedStock(inventory.getReservedStock() + quantity);
                inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
                inventory.setUpdatedAt(now);

                inventoryRepository.save(inventory);

                InventoryHistory history = new InventoryHistory();
                history.setProductId(productId);
                history.setOperationType(InventoryOperationType.RESERVE);
                history.setQuantity(quantity);
                history.setCreatedAt(now);

                historyRepository.save(history);
            }
        }

        return new OrderReserveResponse(request.getOrderId(), responseItems);
    }


    @Override
    public void releaseStock(UUID productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException(
                    "Release quantity must be greater than zero"
            );
        }

        if (inventory.getReservedStock() < quantity) {
            throw new ConflictException(
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
    public void releaseOrderStock(List<OrderNormalCancelledEvent.Item> items) {

        if (items == null || items.isEmpty()) {
            return;
        }

        Map<UUID, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (OrderNormalCancelledEvent.Item item : items) {
            if (item.getProductId() == null) {
                throw new BadRequestException("Product id is required");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException(
                        "Release quantity must be greater than zero for product: "
                                + item.getProductId()
                );
            }

            quantitiesByProduct.merge(
                    item.getProductId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }

        for (Map.Entry<UUID, Integer> entry : quantitiesByProduct.entrySet()) {
            releaseStock(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void deductOrderStock(List<OrderCreatedEvent.Item> items) {

        if (items == null || items.isEmpty()) {
            return;
        }

        Map<UUID, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (OrderCreatedEvent.Item item : items) {
            if (item.getProductId() == null) {
                throw new BadRequestException("Product id is required");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException(
                        "Deduct quantity must be greater than zero for product: "
                                + item.getProductId()
                );
            }

            quantitiesByProduct.merge(
                    item.getProductId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }

        for (Map.Entry<UUID, Integer> entry : quantitiesByProduct.entrySet()) {
            deductStock(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void deductStock(UUID productId, Integer quantity) {


        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException(
                    "Deduct quantity must be greater than zero"
            );
        }

        if (inventory.getReservedStock() < quantity) {
            throw new ConflictException(
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
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException(
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
                .orElseThrow(() -> new InventoryNotFoundException(productId));
    }

    @Override
    public List<Inventory> getInventoryBulk(List<UUID> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            throw new BadRequestException("At least one product id is required");
        }

        if (productIds.stream().anyMatch(id -> id == null)) {
            throw new BadRequestException("Product id is required");
        }

        return inventoryRepository.findAllById(productIds);
    }

    @Override
    public void adjustInventory(UUID productId, Integer adjustment) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        if (adjustment == null) {
            throw new BadRequestException("Adjustment is required");
        }

        int newTotal = inventory.getTotalStock() + adjustment;
        if (newTotal < 0) {
            throw new BadRequestException(
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
            throw new InventoryNotFoundException(productId);
        }

        OffsetDateTime now = OffsetDateTime.now();

        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setOperationType(InventoryOperationType.DELETE);
        history.setQuantity(0);
        history.setCreatedAt(now);

        historyRepository.save(history);
    }

}

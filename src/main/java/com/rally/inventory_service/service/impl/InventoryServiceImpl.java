package com.rally.inventory_service.service.impl;

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
import com.rally.inventory_service.event.OrderCancelledEvent;
import com.rally.inventory_service.event.OrderCreatedEvent;
import com.rally.inventory_service.repository.InventoryHistoryRepository;
import com.rally.inventory_service.repository.InventoryRepository;
import com.rally.inventory_service.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository historyRepository;
    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryHistoryRepository historyRepository) {

        this.inventoryRepository = inventoryRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void createInventory(UUID productId, Integer initialStock) {
        log.debug("Creating inventory for product={} initialStock={}", productId, initialStock);

        if (productId == null) {
            log.warn("createInventory rejected: product id is null");
            throw new BadRequestException("Product id is required");
        }

        if (initialStock == null || initialStock < 0) {
            log.warn("createInventory rejected for product={}: invalid initialStock={}", productId, initialStock);
            throw new BadRequestException("Initial stock must be zero or greater");
        }

        if (inventoryRepository.existsById(productId)) {
            log.warn("createInventory rejected: inventory already exists for product={}", productId);
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

        log.info("Created inventory for product={} initialStock={}", productId, initialStock);
    }


    @Override
    public DealReserveResponse reserveStock(UUID productId, Integer quantity) {
        log.debug("Reserving stock for product={} quantity={}", productId, quantity);

        Inventory inventory = inventoryRepository.findById(productId).orElse(null);
        if (inventory == null) {
            log.warn("reserveStock failed: inventory not found for product={}", productId);
            return DealReserveResponse.notFound();
        }

        if (quantity <= 0) {
            log.warn("reserveStock rejected for product={}: invalid quantity={}", productId, quantity);
            return new DealReserveResponse(false, "INVALID_QUANTITY", inventory.getAvailableStock());
        }

        if (inventory.getAvailableStock() < quantity) {
            log.warn("reserveStock failed for product={}: requested={} available={}",
                    productId, quantity, inventory.getAvailableStock());
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

        log.info("Reserved stock for product={} quantity={} remainingAvailable={}",
                productId, quantity, inventory.getAvailableStock());

        return DealReserveResponse.ok();
    }

    @Override
    public OrderReserveResponse reserveOrder(OrderReserveRequest request) {

        if (request == null || request.getOrderId() == null) {
            log.warn("reserveOrder rejected: order id is null");
            throw new BadRequestException("Order id is required");
        }

        log.debug("Reserving order={} with {} item(s)", request.getOrderId(),
                request.getItems() == null ? 0 : request.getItems().size());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.warn("reserveOrder rejected for order={}: no items provided", request.getOrderId());
            throw new BadRequestException("At least one order item is required");
        }

        for (OrderReserveRequest.Item item : request.getItems()) {
            if (item.getProductId() == null) {
                log.warn("reserveOrder rejected for order={}: product id is null", request.getOrderId());
                throw new BadRequestException("Product id is required");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                log.warn("reserveOrder rejected for order={}: invalid quantity for product={}",
                        request.getOrderId(), item.getProductId());
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
                log.warn("reserveOrder order={}: inventory not found for product={}",
                        request.getOrderId(), productId);
                allAvailable = false;
                availableStockMap.put(productId, 0);
            } else {
                Inventory inventory = inventoryOpt.get();
                inventories.put(productId, inventory);
                availableStockMap.put(productId, inventory.getAvailableStock());
                if (inventory.getAvailableStock() < requestedQuantity) {
                    log.warn("reserveOrder order={}: insufficient stock for product={} requested={} available={}",
                            request.getOrderId(), productId, requestedQuantity, inventory.getAvailableStock());
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

            log.info("Reserved order={} successfully for {} product(s)",
                    request.getOrderId(), totalRequestedPerProduct.size());
        } else {
            log.warn("Order={} could not be fully reserved; no stock was deducted", request.getOrderId());
        }

        return new OrderReserveResponse(request.getOrderId(), responseItems);
    }


    @Override
    public void releaseStock(UUID productId, Integer quantity) {
        log.debug("Releasing stock for product={} quantity={}", productId, quantity);

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("releaseStock failed: inventory not found for product={}", productId);
                    return new InventoryNotFoundException(productId);
                });

        if (quantity == null || quantity <= 0) {
            log.warn("releaseStock rejected for product={}: invalid quantity={}", productId, quantity);
            throw new BadRequestException(
                    "Release quantity must be greater than zero"
            );
        }

        if (inventory.getReservedStock() < quantity) {
            log.warn("releaseStock rejected for product={}: requested={} reserved={}",
                    productId, quantity, inventory.getReservedStock());
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

        log.info("Released stock for product={} quantity={} newAvailable={}",
                productId, quantity, inventory.getAvailableStock());
    }

    @Override
    public void releaseOrderStock(List<OrderCancelledEvent.Item> items) {

        if (items == null || items.isEmpty()) {
            log.debug("releaseOrderStock called with no items; skipping");
            return;
        }

        log.debug("Releasing order stock for {} item(s)", items.size());

        Map<UUID, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (OrderCancelledEvent.Item item : items) {
            if (item.getProductId() == null) {
                log.warn("releaseOrderStock rejected: product id is null");
                throw new BadRequestException("Product id is required");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                log.warn("releaseOrderStock rejected for product={}: invalid quantity", item.getProductId());
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

        log.info("Released order stock for {} product(s)", quantitiesByProduct.size());
    }

    @Override
    public void deductOrderStock(List<OrderCreatedEvent.Item> items) {

        if (items == null || items.isEmpty()) {
            log.debug("deductOrderStock called with no items; skipping");
            return;
        }

        log.debug("Deducting order stock for {} item(s)", items.size());

        Map<UUID, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (OrderCreatedEvent.Item item : items) {
            if (item.getProductId() == null) {
                log.warn("deductOrderStock rejected: product id is null");
                throw new BadRequestException("Product id is required");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                log.warn("deductOrderStock rejected for product={}: invalid quantity", item.getProductId());
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

        log.info("Deducted order stock for {} product(s)", quantitiesByProduct.size());
    }

    @Override
    public void deductStock(UUID productId, Integer quantity) {
        log.debug("Deducting stock for product={} quantity={}", productId, quantity);

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("deductStock failed: inventory not found for product={}", productId);
                    return new InventoryNotFoundException(productId);
                });

        if (quantity == null || quantity <= 0) {
            log.warn("deductStock rejected for product={}: invalid quantity={}", productId, quantity);
            throw new BadRequestException(
                    "Deduct quantity must be greater than zero"
            );
        }

        if (inventory.getReservedStock() < quantity) {
            log.warn("deductStock rejected for product={}: requested={} reserved={}",
                    productId, quantity, inventory.getReservedStock());
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

        log.info("Deducted stock for product={} quantity={} newTotal={}",
                productId, quantity, inventory.getTotalStock());
    }

    @Override
    public void restock(UUID productId, Integer quantity) {
        log.debug("Restocking product={} quantity={}", productId, quantity);

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("restock failed: inventory not found for product={}", productId);
                    return new InventoryNotFoundException(productId);
                });

        if (quantity == null || quantity <= 0) {
            log.warn("restock rejected for product={}: invalid quantity={}", productId, quantity);
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

        log.info("Restocked product={} quantity={} newTotal={}",
                productId, quantity, inventory.getTotalStock());
    }

    @Override
    public Inventory getInventory(UUID productId) {
        log.debug("Fetching inventory for product={}", productId);

        return inventoryRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("getInventory failed: inventory not found for product={}", productId);
                    return new InventoryNotFoundException(productId);
                });
    }

    @Override
    public List<Inventory> getInventoryBulk(List<UUID> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            log.warn("getInventoryBulk rejected: no product ids provided");
            throw new BadRequestException("At least one product id is required");
        }

        if (productIds.stream().anyMatch(id -> id == null)) {
            log.warn("getInventoryBulk rejected: null product id present in list");
            throw new BadRequestException("Product id is required");
        }

        log.debug("Fetching bulk inventory for {} product(s)", productIds.size());

        List<Inventory> result = inventoryRepository.findAllById(productIds);

        log.info("Fetched {} of {} requested inventory record(s)", result.size(), productIds.size());

        return result;
    }

    @Override
    public void adjustInventory(UUID productId, Integer adjustment) {
        log.debug("Adjusting inventory for product={} adjustment={}", productId, adjustment);

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("adjustInventory failed: inventory not found for product={}", productId);
                    return new InventoryNotFoundException(productId);
                });

        if (adjustment == null) {
            log.warn("adjustInventory rejected for product={}: adjustment is null", productId);
            throw new BadRequestException("Adjustment is required");
        }

        int newTotal = inventory.getTotalStock() + adjustment;
        if (newTotal < 0) {
            log.warn("adjustInventory rejected for product={}: adjustment={} would result in negative stock (currentTotal={})",
                    productId, adjustment, inventory.getTotalStock());
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

        log.info("Adjusted inventory for product={} adjustment={} newTotal={}",
                productId, adjustment, newTotal);
    }

    @Override
    public void deleteInventory(UUID productId) {
        log.debug("Deleting inventory for product={}", productId);

        if (!inventoryRepository.existsById(productId)) {
            log.warn("deleteInventory failed: inventory not found for product={}", productId);
            throw new InventoryNotFoundException(productId);
        }

        OffsetDateTime now = OffsetDateTime.now();

        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setOperationType(InventoryOperationType.DELETE);
        history.setQuantity(0);
        history.setCreatedAt(now);

        historyRepository.save(history);

        log.info("Recorded delete operation for product={}", productId);
    }

}
package com.rally.inventory_service.controller;
import com.rally.inventory_service.dto.DealReserveResponse;
import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.dto.OrderReserveRequest;
import com.rally.inventory_service.dto.OrderReserveResponse;
import com.rally.inventory_service.dto.ReserveInventoryRequest;
import com.rally.inventory_service.dto.RestockRequest;
import com.rally.inventory_service.dto.AdjustRequest;
import com.rally.inventory_service.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/{productId}/reserve-deal")
    public ResponseEntity<DealReserveResponse> reserveDeal(
            @PathVariable UUID productId,
            @RequestBody ReserveInventoryRequest request
    ) {
        log.info("Reserving deal for productId: {}, quantity: {}", productId, request == null ? null : request.getQuantity());
        DealReserveResponse result = inventoryService.reserveStock(
                productId,
                request == null ? null : request.getQuantity()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{productId}/release-deal")
    public ResponseEntity<Void> releaseDeal(
            @PathVariable UUID productId,
            @RequestBody ReserveInventoryRequest request
    ) {
        log.info("Releasing deal for productId: {}, quantity: {}", productId, request == null ? null : request.getQuantity());
        inventoryService.releaseStock(
                productId,
                request == null ? null : request.getQuantity()
        );

        return ResponseEntity.ok().build();
    }
    @PostMapping("/{productId}/reserve-order")
    public ResponseEntity<DealReserveResponse> reserveOrder(
            @PathVariable UUID productId,
            @RequestBody ReserveInventoryRequest request
    ) {
        log.info("Reserving order for productId: {}, quantity: {}", productId, request == null ? null : request.getQuantity());
        DealReserveResponse result = inventoryService.reserveStock(
                productId,
                request == null ? null : request.getQuantity()
        );
        return ResponseEntity.ok(result);
    }
    @PostMapping("/{productId}/release-order")
    public ResponseEntity<Void> releaseOrder(
            @PathVariable UUID productId,
            @RequestBody ReserveInventoryRequest request
    ) {
        log.info("Releasing order for productId: {}, quantity: {}", productId, request == null ? null : request.getQuantity());
        inventoryService.releaseStock(
                productId,
                request == null ? null : request.getQuantity()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/order-reserve")
    public ResponseEntity<OrderReserveResponse> reserveOrder(
            @RequestBody OrderReserveRequest request
    ) {
        log.info("Reserving order for orderId: {}, items: {}", request == null ? null : request.getOrderId(), request == null ? null : request.getItems());
        return ResponseEntity.ok(inventoryService.reserveOrder(request));
    }

    @PatchMapping("/{productId}/restock")
    public ResponseEntity<Map<String, String>> restock(
            @PathVariable UUID productId,
            @RequestBody RestockRequest request
    ) {
        log.info("Restocking inventory for productId: {}, quantity: {}", productId, request == null ? null : request.getQuantity());
        inventoryService.restock(productId, request == null ? null : request.getQuantity());

        return ResponseEntity.ok(Map.of("message", "Inventory restocked successfully."));
    }

    @PatchMapping("/{productId}/adjust")
    public ResponseEntity<Map<String, String>> adjust(
            @PathVariable UUID productId,
            @RequestBody AdjustRequest request
    ) {
        log.info("Adjusting inventory for productId: {}, adjustment: {}", productId, request == null ? null : request.getAdjustment());
        inventoryService.adjustInventory(productId, request == null ? null : request.getAdjustment());

        return ResponseEntity.ok(Map.of("message", "Inventory adjusted successfully."));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable UUID productId
    ) {
        log.info("Deleting inventory for productId: {}", productId);
        inventoryService.deleteInventory(productId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(
            @PathVariable UUID productId
    ) {
        log.info("Getting inventory for productId: {}", productId);
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Inventory>> getInventoryBulk(
            @RequestBody List<UUID> productIds
    ) {
        log.info("Getting inventory for productIds: {}", productIds);
        List<Inventory> items = inventoryService.getInventoryBulk(productIds);
        Map<String, Inventory> result = items.stream()
                .collect(Collectors.toMap(inv -> inv.getProductId().toString(), inv -> inv));
        return ResponseEntity.ok(result);
    }
}

package com.rally.inventory_service.controller;
import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.dto.OrderReserveRequest;
import com.rally.inventory_service.dto.OrderReserveResponse;
import com.rally.inventory_service.dto.ReserveInventoryRequest;
import com.rally.inventory_service.dto.RestockRequest;
import com.rally.inventory_service.dto.AdjustRequest;
import com.rally.inventory_service.service.InventoryService;
import com.rally.inventory_service.repository.InventoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryService inventoryService, InventoryRepository inventoryRepository) {
        this.inventoryService = inventoryService;
        this.inventoryRepository = inventoryRepository;
    }

    @PostMapping("/{productId}/reserve-deal")
    public ResponseEntity<Void> reserveDeal(
            @PathVariable UUID productId,
            @RequestBody ReserveInventoryRequest request
    ) {

        inventoryService.reserveStock(
                productId,
                request.getQuantity()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/release-deal")
    public ResponseEntity<Void> releaseDeal(
            @PathVariable UUID productId,
            @RequestBody ReserveInventoryRequest request
    ) {

        inventoryService.releaseStock(
                productId,
                request.getQuantity()
        );

        return ResponseEntity.ok().build();
    }
    @PostMapping("/{productId}/reserve-order")
    public ResponseEntity<Void> reserveOrder(
            @PathVariable UUID productId,
            @RequestBody ReserveInventoryRequest request
    ) {

        inventoryService.reserveStock(
                productId,
                request.getQuantity()
        );

        return ResponseEntity.ok().build();
    }
    @PostMapping("/{productId}/release-order")
    public ResponseEntity<Void> releaseOrder(
            @PathVariable UUID productId,
            @RequestBody ReserveInventoryRequest request
    ) {

        inventoryService.releaseStock(
                productId,
                request.getQuantity()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/order-reserve")
    public ResponseEntity<OrderReserveResponse> reserveOrder(
            @RequestBody OrderReserveRequest request
    ) {

        return ResponseEntity.ok(inventoryService.reserveOrder(request));
    }

    @PatchMapping("/{productId}/restock")
    public ResponseEntity<Map<String, String>> restock(
            @PathVariable UUID productId,
            @RequestBody RestockRequest request
    ) {

        inventoryService.restock(productId, request.getQuantity());

        return ResponseEntity.ok(Map.of("message", "Inventory restocked successfully."));
    }

    @PatchMapping("/{productId}/adjust")
    public ResponseEntity<Map<String, String>> adjust(
            @PathVariable UUID productId,
            @RequestBody AdjustRequest request
    ) {

        inventoryService.adjustInventory(productId, request.getAdjustment());

        return ResponseEntity.ok(Map.of("message", "Inventory adjusted successfully."));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable UUID productId
    ) {

        inventoryService.deleteInventory(productId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(
            @PathVariable UUID productId
    ) {
        return inventoryRepository.findById(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Inventory>> getInventoryBulk(
            @RequestBody List<UUID> productIds
    ) {
        List<Inventory> items = inventoryRepository.findAllById(productIds);
        Map<String, Inventory> result = items.stream()
                .collect(Collectors.toMap(inv -> inv.getProductId().toString(), inv -> inv));
        return ResponseEntity.ok(result);
    }
}

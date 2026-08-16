package com.rally.inventory_service.controller;
import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.dto.ReserveInventoryRequest;
import com.rally.inventory_service.dto.RestockRequest;
import com.rally.inventory_service.dto.AdjustRequest;
import com.rally.inventory_service.service.InventoryService;
import com.rally.inventory_service.repository.InventoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

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
}
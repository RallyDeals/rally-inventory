package com.rally.inventory_service.controller;
import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.dto.ReserveInventoryRequest;
import com.rally.inventory_service.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
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
    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(
                inventoryService.getInventory(productId)
        );
    }
}
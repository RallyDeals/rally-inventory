package com.rally.inventory_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rally.inventory_service.dto.*;
import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private ObjectMapper objectMapper;
    private UUID productId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("POST /inventory/{productId}/reserve-deal should return 200 OK with DealReserveResponse")
    void testReserveDeal() throws Exception {
        ReserveInventoryRequest request = new ReserveInventoryRequest(5);
        DealReserveResponse response = DealReserveResponse.ok();

        when(inventoryService.reserveStock(productId, 5)).thenReturn(response);

        mockMvc.perform(post("/inventory/{productId}/reserve-deal", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(inventoryService).reserveStock(productId, 5);
    }

    @Test
    @DisplayName("POST /inventory/{productId}/release-deal should return 200 OK")
    void testReleaseDeal() throws Exception {
        ReserveInventoryRequest request = new ReserveInventoryRequest(5);

        mockMvc.perform(post("/inventory/{productId}/release-deal", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(inventoryService).releaseStock(productId, 5);
    }

    @Test
    @DisplayName("POST /inventory/{productId}/reserve-order should return 200 OK with DealReserveResponse")
    void testReserveOrderSingle() throws Exception {
        ReserveInventoryRequest request = new ReserveInventoryRequest(3);
        DealReserveResponse response = DealReserveResponse.ok();

        when(inventoryService.reserveStock(productId, 3)).thenReturn(response);

        mockMvc.perform(post("/inventory/{productId}/reserve-order", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(inventoryService).reserveStock(productId, 3);
    }

    @Test
    @DisplayName("POST /inventory/{productId}/release-order should return 200 OK")
    void testReleaseOrderSingle() throws Exception {
        ReserveInventoryRequest request = new ReserveInventoryRequest(3);

        mockMvc.perform(post("/inventory/{productId}/release-order", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(inventoryService).releaseStock(productId, 3);
    }

    @Test
    @DisplayName("POST /inventory/order-reserve should return 200 OK with OrderReserveResponse")
    void testReserveOrderBatch() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderReserveRequest request = new OrderReserveRequest(
                orderId,
                List.of(new OrderReserveRequest.Item(productId, 2))
        );
        OrderReserveResponse response = new OrderReserveResponse(
                orderId,
                List.of(new OrderReserveResponse.Item(productId, 50, true))
        );

        when(inventoryService.reserveOrder(any(OrderReserveRequest.class))).thenReturn(response);

        mockMvc.perform(post("/inventory/order-reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.items[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.items[0].reserved").value(true));

        verify(inventoryService).reserveOrder(any(OrderReserveRequest.class));
    }

    @Test
    @DisplayName("PATCH /inventory/{productId}/restock should return 200 OK with confirmation message")
    void testRestock() throws Exception {
        RestockRequest request = new RestockRequest(25);

        mockMvc.perform(patch("/inventory/{productId}/restock", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventory restocked successfully."));

        verify(inventoryService).restock(productId, 25);
    }

    @Test
    @DisplayName("PATCH /inventory/{productId}/adjust should return 200 OK with confirmation message")
    void testAdjust() throws Exception {
        AdjustRequest request = new AdjustRequest(-10, "Inventory correction");

        mockMvc.perform(patch("/inventory/{productId}/adjust", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventory adjusted successfully."));

        verify(inventoryService).adjustInventory(productId, -10);
    }

    @Test
    @DisplayName("DELETE /inventory/{productId} should return 204 No Content")
    void testDeleteInventory() throws Exception {
        mockMvc.perform(delete("/inventory/{productId}", productId))
                .andExpect(status().isNoContent());

        verify(inventoryService).deleteInventory(productId);
    }

    @Test
    @DisplayName("GET /inventory/{productId} should return 200 OK with inventory details")
    void testGetInventory() throws Exception {
        Inventory inventory = new Inventory(productId, 100, 20, 80, 1L, OffsetDateTime.now());
        when(inventoryService.getInventory(productId)).thenReturn(inventory);

        mockMvc.perform(get("/inventory/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.totalStock").value(100))
                .andExpect(jsonPath("$.reservedStock").value(20))
                .andExpect(jsonPath("$.availableStock").value(80));

        verify(inventoryService).getInventory(productId);
    }

    @Test
    @DisplayName("POST /inventory/bulk should return 200 OK with map of inventories")
    void testGetInventoryBulk() throws Exception {
        Inventory inventory = new Inventory(productId, 100, 20, 80, 1L, OffsetDateTime.now());
        when(inventoryService.getInventoryBulk(List.of(productId))).thenReturn(List.of(inventory));

        mockMvc.perform(post("/inventory/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(productId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['" + productId.toString() + "'].totalStock").value(100))
                .andExpect(jsonPath("$.['" + productId.toString() + "'].availableStock").value(80));

        verify(inventoryService).getInventoryBulk(List.of(productId));
    }
}

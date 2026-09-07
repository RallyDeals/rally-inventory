package com.rally.inventory_service.observability;

import com.rally.inventory_service.controller.InventoryController;
import com.rally.inventory_service.entity.Inventory;
import com.rally.inventory_service.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HttpDistributedTracingTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
    }

    @Test
    @DisplayName("Verify HTTP request with traceparent and X-Correlation-Id is processed cleanly")
    void testIncomingHttpRequestWithTracingHeaders() throws Exception {
        UUID productId = UUID.randomUUID();
        Inventory inventory = new Inventory(productId, 100, 20, 80, 1L, OffsetDateTime.now());
        when(inventoryService.getInventory(productId)).thenReturn(inventory);

        mockMvc.perform(get("/inventory/{productId}", productId)
                        .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                        .header("X-Correlation-Id", "corr-test-123"))
                .andExpect(status().isOk());

        verify(inventoryService).getInventory(productId);
    }
}

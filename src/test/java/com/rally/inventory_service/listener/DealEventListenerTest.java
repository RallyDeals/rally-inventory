package com.rally.inventory_service.listener;

import com.rally.inventory_service.event.DealEvent;
import com.rally.inventory_service.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealEventListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private DealEventListener dealEventListener;

    private UUID productId;
    private DealEvent dealEvent;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        dealEvent = new DealEvent();
        dealEvent.setProductId(productId);
        dealEvent.setQuantity(10);
    }

    @Test
    @DisplayName("Should call reserveStock on Deal.Created event")
    void handleDealCreated() {
        dealEventListener.handleDealEvent(dealEvent, "deal-123", "Deal.Created");
        verify(inventoryService).reserveStock(productId, 10);
    }

    @Test
    @DisplayName("Should call releaseStock on Deal.Cancelled event")
    void handleDealCancelled() {
        dealEventListener.handleDealEvent(dealEvent, "deal-123", "Deal.Cancelled");
        verify(inventoryService).releaseStock(productId, 10);
    }

    @Test
    @DisplayName("Should call releaseStock on Deal.Failed event")
    void handleDealFailed() {
        dealEventListener.handleDealEvent(dealEvent, "deal-123", "Deal.Failed");
        verify(inventoryService).releaseStock(productId, 10);
    }

    @Test
    @DisplayName("Should handle Deal.Succeeded event")
    void handleDealSucceeded() {
        dealEventListener.handleDealEvent(dealEvent, "deal-123", "Deal.Succeeded");
        verify(inventoryService).releaseStock(productId, 10);
    }

    @Test
    @DisplayName("Should do nothing on Deal.Activated event")
    void handleDealActivated() {
        dealEventListener.handleDealEvent(dealEvent, "deal-123", "Deal.Activated");
        verifyNoInteractions(inventoryService);
    }

    @Test
    @DisplayName("Should ignore unknown event types")
    void handleUnknownEventType() {
        dealEventListener.handleDealEvent(dealEvent, "deal-123", "Unknown.Type");
        verifyNoInteractions(inventoryService);
    }
}

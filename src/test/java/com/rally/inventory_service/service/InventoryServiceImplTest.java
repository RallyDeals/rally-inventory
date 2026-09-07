package com.rally.inventory_service.service;

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
import com.rally.inventory_service.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository historyRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private UUID productId;
    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        sampleInventory = new Inventory(
                productId,
                100,
                20,
                80,
                1L,
                OffsetDateTime.now()
        );
    }

    @Nested
    @DisplayName("createInventory tests")
    class CreateInventoryTests {

        @Test
        @DisplayName("Should create inventory and history record successfully")
        void shouldCreateInventorySuccessfully() {
            when(inventoryRepository.existsById(productId)).thenReturn(false);

            inventoryService.createInventory(productId, 50);

            ArgumentCaptor<Inventory> invCaptor = ArgumentCaptor.forClass(Inventory.class);
            verify(inventoryRepository).save(invCaptor.capture());
            Inventory saved = invCaptor.getValue();
            assertThat(saved.getProductId()).isEqualTo(productId);
            assertThat(saved.getTotalStock()).isEqualTo(50);
            assertThat(saved.getAvailableStock()).isEqualTo(50);
            assertThat(saved.getReservedStock()).isEqualTo(0);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            InventoryHistory savedHist = histCaptor.getValue();
            assertThat(savedHist.getProductId()).isEqualTo(productId);
            assertThat(savedHist.getOperationType()).isEqualTo(InventoryOperationType.CREATE);
            assertThat(savedHist.getQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw BadRequestException when productId is null")
        void shouldThrowWhenProductIdIsNull() {
            assertThatThrownBy(() -> inventoryService.createInventory(null, 10))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Product id is required");
            verifyNoInteractions(inventoryRepository, historyRepository);
        }

        @Test
        @DisplayName("Should throw BadRequestException when initialStock is negative or null")
        void shouldThrowWhenInitialStockIsInvalid() {
            assertThatThrownBy(() -> inventoryService.createInventory(productId, -5))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Initial stock must be zero or greater");

            assertThatThrownBy(() -> inventoryService.createInventory(productId, null))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(inventoryRepository, historyRepository);
        }

        @Test
        @DisplayName("Should throw AlreadyExistsException when inventory already exists")
        void shouldThrowWhenInventoryAlreadyExists() {
            when(inventoryRepository.existsById(productId)).thenReturn(true);

            assertThatThrownBy(() -> inventoryService.createInventory(productId, 10))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessageContaining("already exists");
            verify(historyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reserveStock tests")
    class ReserveStockTests {

        @Test
        @DisplayName("Should successfully reserve stock and record history")
        void shouldReserveStockSuccessfully() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            DealReserveResponse response = inventoryService.reserveStock(productId, 30);

            assertThat(response.isSuccess()).isTrue();
            assertThat(sampleInventory.getReservedStock()).isEqualTo(50);
            assertThat(sampleInventory.getAvailableStock()).isEqualTo(50);
            verify(inventoryRepository).save(sampleInventory);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.RESERVE);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should return NOT_FOUND response when product not found")
        void shouldReturnNotFoundWhenProductDoesNotExist() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.empty());

            DealReserveResponse response = inventoryService.reserveStock(productId, 10);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getReason()).isEqualTo("PRODUCT_NOT_FOUND");
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return INVALID_QUANTITY response when quantity <= 0")
        void shouldReturnInvalidQuantityWhenZeroOrNegative() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            DealReserveResponse response = inventoryService.reserveStock(productId, 0);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getReason()).isEqualTo("INVALID_QUANTITY");
            assertThat(response.getAvailableStock()).isEqualTo(80);
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return INVALID_QUANTITY response when quantity is null")
        void shouldReturnInvalidQuantityWhenQuantityIsNull() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            DealReserveResponse response = inventoryService.reserveStock(productId, null);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getReason()).isEqualTo("INVALID_QUANTITY");
            assertThat(response.getAvailableStock()).isEqualTo(80);
            verify(inventoryRepository, never()).save(any());
            verifyNoInteractions(historyRepository);
        }

        @Test
        @DisplayName("Should return INSUFFICIENT_STOCK response when requested > available")
        void shouldReturnInsufficientStockWhenNotEnough() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            DealReserveResponse response = inventoryService.reserveStock(productId, 100);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getReason()).isEqualTo("INSUFFICIENT_STOCK");
            assertThat(response.getAvailableStock()).isEqualTo(80);
            verify(inventoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reserveOrder (Atomic multi-item) tests")
    class ReserveOrderTests {

        private UUID orderId;
        private UUID product1;
        private UUID product2;

        @BeforeEach
        void init() {
            orderId = UUID.randomUUID();
            product1 = UUID.randomUUID();
            product2 = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should reserve all items atomically when all are available")
        void shouldReserveAllItemsWhenAvailable() {
            Inventory inv1 = new Inventory(product1, 50, 0, 50, 1L, OffsetDateTime.now());
            Inventory inv2 = new Inventory(product2, 30, 0, 30, 1L, OffsetDateTime.now());

            when(inventoryRepository.findById(product1)).thenReturn(Optional.of(inv1));
            when(inventoryRepository.findById(product2)).thenReturn(Optional.of(inv2));

            OrderReserveRequest request = new OrderReserveRequest(
                    orderId,
                    List.of(
                            new OrderReserveRequest.Item(product1, 5),
                            new OrderReserveRequest.Item(product2, 10)
                    )
            );

            OrderReserveResponse response = inventoryService.reserveOrder(request);

            assertThat(response.getOrderId()).isEqualTo(orderId);
            assertThat(response.getItems()).hasSize(2);
            assertThat(response.getItems()).allMatch(item -> Boolean.TRUE.equals(item.getReserved()));

            assertThat(inv1.getAvailableStock()).isEqualTo(45);
            assertThat(inv1.getReservedStock()).isEqualTo(5);
            assertThat(inv2.getAvailableStock()).isEqualTo(20);
            assertThat(inv2.getReservedStock()).isEqualTo(10);

            verify(inventoryRepository, times(2)).save(any(Inventory.class));
            verify(historyRepository, times(2)).save(any(InventoryHistory.class));
        }

        @Test
        @DisplayName("Should NOT write any stock changes when one item is insufficient (atomic batch guarantee)")
        void shouldNotReserveWhenOneItemIsInsufficient() {
            Inventory inv1 = new Inventory(product1, 50, 0, 50, 1L, OffsetDateTime.now());
            Inventory inv2 = new Inventory(product2, 5, 0, 5, 1L, OffsetDateTime.now());

            when(inventoryRepository.findById(product1)).thenReturn(Optional.of(inv1));
            when(inventoryRepository.findById(product2)).thenReturn(Optional.of(inv2));

            OrderReserveRequest request = new OrderReserveRequest(
                    orderId,
                    List.of(
                            new OrderReserveRequest.Item(product1, 5),
                            new OrderReserveRequest.Item(product2, 10) // requests 10, only 5 available
                    )
            );

            OrderReserveResponse response = inventoryService.reserveOrder(request);

            assertThat(response.getItems()).hasSize(2);

            // product1 has enough stock individually → its per-item flag shows reserved=true
            // but NO stock was actually written because the batch failed
            assertThat(response.getItems().get(0).getReserved()).isTrue();

            // product2 does NOT have enough stock → its per-item flag shows reserved=false
            assertThat(response.getItems().get(1).getReserved()).isFalse();

            // Key atomic guarantee: stocks remain completely untouched, nothing persisted
            assertThat(inv1.getAvailableStock()).isEqualTo(50);
            assertThat(inv2.getAvailableStock()).isEqualTo(5);
            verify(inventoryRepository, never()).save(any());
            verify(historyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle missing product in batch request")
        void shouldHandleMissingProductInBatch() {
            Inventory inv1 = new Inventory(product1, 50, 0, 50, 1L, OffsetDateTime.now());

            when(inventoryRepository.findById(product1)).thenReturn(Optional.of(inv1));
            when(inventoryRepository.findById(product2)).thenReturn(Optional.empty());

            OrderReserveRequest request = new OrderReserveRequest(
                    orderId,
                    List.of(
                            new OrderReserveRequest.Item(product1, 5),
                            new OrderReserveRequest.Item(product2, 5)
                    )
            );

            OrderReserveResponse response = inventoryService.reserveOrder(request);

            assertThat(response.getItems().get(1).getAvailable()).isEqualTo(0);
            assertThat(response.getItems().get(1).getReserved()).isFalse();
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should aggregate requested quantity for duplicate products in single request")
        void shouldAggregateDuplicateProductsInRequest() {
            Inventory inv1 = new Inventory(product1, 10, 0, 10, 1L, OffsetDateTime.now());
            when(inventoryRepository.findById(product1)).thenReturn(Optional.of(inv1));

            OrderReserveRequest request = new OrderReserveRequest(
                    orderId,
                    List.of(
                            new OrderReserveRequest.Item(product1, 6),
                            new OrderReserveRequest.Item(product1, 6) // Total 12 requested, only 10 available
                    )
            );

            OrderReserveResponse response = inventoryService.reserveOrder(request);

            assertThat(response.getItems()).allMatch(item -> Boolean.FALSE.equals(item.getReserved()));
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException on invalid requests")
        void shouldThrowBadRequestOnInvalidReserveOrderRequests() {
            assertThatThrownBy(() -> inventoryService.reserveOrder(null))
                    .isInstanceOf(BadRequestException.class);

            assertThatThrownBy(() -> inventoryService.reserveOrder(new OrderReserveRequest(null, List.of())))
                    .isInstanceOf(BadRequestException.class);

            assertThatThrownBy(() -> inventoryService.reserveOrder(new OrderReserveRequest(orderId, List.of())))
                    .isInstanceOf(BadRequestException.class);

            assertThatThrownBy(() -> inventoryService.reserveOrder(new OrderReserveRequest(orderId, List.of(new OrderReserveRequest.Item(null, 5)))))
                    .isInstanceOf(BadRequestException.class);

            assertThatThrownBy(() -> inventoryService.reserveOrder(new OrderReserveRequest(orderId, List.of(new OrderReserveRequest.Item(product1, 0)))))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("releaseStock tests")
    class ReleaseStockTests {

        @Test
        @DisplayName("Should successfully release stock and record history")
        void shouldReleaseStockSuccessfully() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            inventoryService.releaseStock(productId, 10);

            assertThat(sampleInventory.getReservedStock()).isEqualTo(10);
            assertThat(sampleInventory.getAvailableStock()).isEqualTo(90);
            verify(inventoryRepository).save(sampleInventory);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.RELEASE);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should throw ConflictException when releasing more than reserved")
        void shouldThrowConflictWhenReleasingMoreThanReserved() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            assertThatThrownBy(() -> inventoryService.releaseStock(productId, 50))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Cannot release more stock than currently reserved");
        }

        @Test
        @DisplayName("Should throw InventoryNotFoundException when inventory does not exist")
        void shouldThrowNotFoundWhenReleasingNonExistent() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.releaseStock(productId, 5))
                    .isInstanceOf(InventoryNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when quantity is invalid")
        void shouldThrowBadRequestWhenQuantityInvalid() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            assertThatThrownBy(() -> inventoryService.releaseStock(productId, 0))
                    .isInstanceOf(BadRequestException.class);

            assertThatThrownBy(() -> inventoryService.releaseStock(productId, null))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("releaseOrderStock tests")
    class ReleaseOrderStockTests {

        private OrderCancelledEvent.Item createItem(UUID productId, Integer quantity) {
            OrderCancelledEvent.Item item = new OrderCancelledEvent.Item();
            item.setProductId(productId);
            item.setQuantity(quantity);
            return item;
        }

        @Test
        @DisplayName("Should release order items successfully")
        void shouldReleaseOrderStockSuccessfully() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            List<OrderCancelledEvent.Item> items = List.of(
                    createItem(productId, 5),
                    createItem(productId, 5)
            );

            inventoryService.releaseOrderStock(items);

            assertThat(sampleInventory.getReservedStock()).isEqualTo(10);
            assertThat(sampleInventory.getAvailableStock()).isEqualTo(90);
            verify(inventoryRepository).save(sampleInventory);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.RELEASE);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should no-op on null or empty items list")
        void shouldNoOpOnEmptyItems() {
            inventoryService.releaseOrderStock(null);
            inventoryService.releaseOrderStock(List.of());
            verifyNoInteractions(inventoryRepository, historyRepository);
        }

        @Test
        @DisplayName("Should throw BadRequestException on invalid items")
        void shouldThrowOnInvalidItemInList() {
            List<OrderCancelledEvent.Item> nullIdItems = List.of(createItem(null, 5));
            assertThatThrownBy(() -> inventoryService.releaseOrderStock(nullIdItems))
                    .isInstanceOf(BadRequestException.class);

            List<OrderCancelledEvent.Item> negativeQtyItems = List.of(createItem(productId, -1));
            assertThatThrownBy(() -> inventoryService.releaseOrderStock(negativeQtyItems))
                    .isInstanceOf(BadRequestException.class);

            verifyNoInteractions(inventoryRepository, historyRepository);
        }
    }

    @Nested
    @DisplayName("deductStock tests")
    class DeductStockTests {

        @Test
        @DisplayName("Should successfully deduct stock and record history")
        void shouldDeductStockSuccessfully() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            inventoryService.deductStock(productId, 15);

            assertThat(sampleInventory.getTotalStock()).isEqualTo(85);
            assertThat(sampleInventory.getReservedStock()).isEqualTo(5);
            assertThat(sampleInventory.getAvailableStock()).isEqualTo(80);
            verify(inventoryRepository).save(sampleInventory);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.DEDUCT);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should throw ConflictException when deducting more than reserved")
        void shouldThrowConflictWhenDeductingMoreThanReserved() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            assertThatThrownBy(() -> inventoryService.deductStock(productId, 25))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Cannot deduct more stock than currently reserved");
        }

        @Test
        @DisplayName("Should throw InventoryNotFoundException when inventory does not exist")
        void shouldThrowNotFoundWhenDeductingNonExistent() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.deductStock(productId, 5))
                    .isInstanceOf(InventoryNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when quantity is invalid")
        void shouldThrowBadRequestWhenQuantityInvalid() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            assertThatThrownBy(() -> inventoryService.deductStock(productId, 0))
                    .isInstanceOf(BadRequestException.class);

            assertThatThrownBy(() -> inventoryService.deductStock(productId, null))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("deductOrderStock tests")
    class DeductOrderStockTests {

        @Test
        @DisplayName("Should deduct order items successfully")
        void shouldDeductOrderStockSuccessfully() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            List<OrderCreatedEvent.Item> items = List.of(
                    new OrderCreatedEvent.Item(productId, 5),
                    new OrderCreatedEvent.Item(productId, 5)
            );

            inventoryService.deductOrderStock(items);

            assertThat(sampleInventory.getTotalStock()).isEqualTo(90);
            assertThat(sampleInventory.getReservedStock()).isEqualTo(10);
            verify(inventoryRepository).save(sampleInventory);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.DEDUCT);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should no-op on null or empty items list")
        void shouldNoOpOnEmptyItems() {
            inventoryService.deductOrderStock(null);
            inventoryService.deductOrderStock(List.of());
            verifyNoInteractions(inventoryRepository, historyRepository);
        }

        @Test
        @DisplayName("Should throw BadRequestException on invalid items")
        void shouldThrowOnInvalidItemInList() {
            List<OrderCreatedEvent.Item> nullIdItems = List.of(new OrderCreatedEvent.Item(null, 5));
            assertThatThrownBy(() -> inventoryService.deductOrderStock(nullIdItems))
                    .isInstanceOf(BadRequestException.class);

            List<OrderCreatedEvent.Item> negativeQtyItems = List.of(new OrderCreatedEvent.Item(productId, -1));
            assertThatThrownBy(() -> inventoryService.deductOrderStock(negativeQtyItems))
                    .isInstanceOf(BadRequestException.class);

            verifyNoInteractions(inventoryRepository, historyRepository);
        }
    }

    @Nested
    @DisplayName("restock tests")
    class RestockTests {

        @Test
        @DisplayName("Should restock inventory and record history")
        void shouldRestockSuccessfully() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            inventoryService.restock(productId, 50);

            assertThat(sampleInventory.getTotalStock()).isEqualTo(150);
            assertThat(sampleInventory.getAvailableStock()).isEqualTo(130);
            verify(inventoryRepository).save(sampleInventory);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.RESTOCK);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw InventoryNotFoundException when inventory does not exist")
        void shouldThrowNotFoundOnRestock() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.restock(productId, 20))
                    .isInstanceOf(InventoryNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when quantity <= 0 or null")
        void shouldThrowBadRequestOnInvalidRestockQuantity() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            assertThatThrownBy(() -> inventoryService.restock(productId, 0))
                    .isInstanceOf(BadRequestException.class);

            assertThatThrownBy(() -> inventoryService.restock(productId, null))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("adjustInventory tests")
    class AdjustInventoryTests {

        @Test
        @DisplayName("Should adjust inventory positively")
        void shouldAdjustPositively() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            inventoryService.adjustInventory(productId, 20);

            assertThat(sampleInventory.getTotalStock()).isEqualTo(120);
            assertThat(sampleInventory.getAvailableStock()).isEqualTo(100);
            verify(inventoryRepository).save(sampleInventory);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.UPDATE);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should adjust inventory negatively")
        void shouldAdjustNegatively() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            inventoryService.adjustInventory(productId, -20);

            assertThat(sampleInventory.getTotalStock()).isEqualTo(80);
            assertThat(sampleInventory.getAvailableStock()).isEqualTo(60);
            verify(inventoryRepository).save(sampleInventory);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.UPDATE);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should throw BadRequestException if adjustment results in negative total stock")
        void shouldThrowWhenAdjustmentResultsInNegativeStock() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            assertThatThrownBy(() -> inventoryService.adjustInventory(productId, -150))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("negative stock");
        }

        @Test
        @DisplayName("Should throw BadRequestException when adjustment is null")
        void shouldThrowWhenAdjustmentIsNull() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            assertThatThrownBy(() -> inventoryService.adjustInventory(productId, null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should throw InventoryNotFoundException when inventory does not exist")
        void shouldThrowWhenAdjustingNonExistent() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.adjustInventory(productId, 5))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteInventory tests")
    class DeleteInventoryTests {

        @Test
        @DisplayName("Should record delete in history")
        void shouldDeleteInventorySuccessfully() {
            when(inventoryRepository.existsById(productId)).thenReturn(true);

            inventoryService.deleteInventory(productId);

            ArgumentCaptor<InventoryHistory> histCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
            verify(historyRepository).save(histCaptor.capture());
            assertThat(histCaptor.getValue().getOperationType()).isEqualTo(InventoryOperationType.DELETE);
            assertThat(histCaptor.getValue().getQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw InventoryNotFoundException when deleting non-existent")
        void shouldThrowNotFoundOnDelete() {
            when(inventoryRepository.existsById(productId)).thenReturn(false);

            assertThatThrownBy(() -> inventoryService.deleteInventory(productId))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getInventory & getInventoryBulk tests")
    class GetInventoryTests {

        @Test
        @DisplayName("Should get single inventory")
        void shouldGetSingleInventory() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(sampleInventory));

            Inventory result = inventoryService.getInventory(productId);

            assertThat(result).isEqualTo(sampleInventory);
        }

        @Test
        @DisplayName("Should throw InventoryNotFoundException when getInventory not found")
        void shouldThrowNotFoundOnGet() {
            when(inventoryRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.getInventory(productId))
                    .isInstanceOf(InventoryNotFoundException.class);
        }

        @Test
        @DisplayName("Should get bulk inventories")
        void shouldGetBulkInventories() {
            UUID prod2 = UUID.randomUUID();
            Inventory inv2 = new Inventory(prod2, 50, 0, 50, 1L, OffsetDateTime.now());

            when(inventoryRepository.findAllById(List.of(productId, prod2)))
                    .thenReturn(List.of(sampleInventory, inv2));

            List<Inventory> result = inventoryService.getInventoryBulk(List.of(productId, prod2));

            assertThat(result).containsExactlyInAnyOrder(sampleInventory, inv2);
        }

        @Test
        @DisplayName("Should throw BadRequestException on null or empty bulk list")
        void shouldThrowOnEmptyBulkList() {
            assertThatThrownBy(() -> inventoryService.getInventoryBulk(null))
                    .isInstanceOf(BadRequestException.class);

            assertThatThrownBy(() -> inventoryService.getInventoryBulk(List.of()))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when bulk list contains null element")
        void shouldThrowOnBulkListWithNullElement() {
            List<UUID> listWithNull = new java.util.ArrayList<>();
            listWithNull.add(productId);
            listWithNull.add(null);

            assertThatThrownBy(() -> inventoryService.getInventoryBulk(listWithNull))
                    .isInstanceOf(BadRequestException.class);
        }
    }
}

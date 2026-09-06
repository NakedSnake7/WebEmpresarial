package com.webempresarial.store.commerce.application.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.webempresarial.store.commerce.domain.inventory.InventoryMovement;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryMovementRepository;
import com.webempresarial.store.dto.inventory.InventoryDashboardDTO;
import com.webempresarial.store.dto.inventory.InventoryProductStockDTO;
import com.webempresarial.store.dto.inventory.InventoryTopMovementDTO;
import com.webempresarial.store.dto.inventory.StockSeverity;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;

@ExtendWith(MockitoExtension.class)
class InventoryDashboardServiceTest {

    @Mock
    private InventoryStockQueryGateway inventoryStockQueryGateway;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private InventoryAlertService inventoryAlertService;

    private InventoryDashboardService service;

    private Store store;

    @BeforeEach
    void setUp() {

        service = new InventoryDashboardService(
                inventoryStockQueryGateway,
                movementRepository,
                inventoryAlertService
        );

        store = new Store();
        store.setId(1L);
    }

    @Test
    void getDashboard_shouldRejectNullStore() {

        assertThatThrownBy(() ->
                service.getDashboard(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La tienda es obligatoria");

        verifyNoInteractions(
                inventoryStockQueryGateway,
                movementRepository,
                inventoryAlertService
        );
    }

    @Test
    void getDashboard_shouldRejectStoreWithoutId() {

        Store invalidStore = new Store();

        assertThatThrownBy(() ->
                service.getDashboard(invalidStore)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La tienda es obligatoria");

        verifyNoInteractions(
                inventoryStockQueryGateway,
                movementRepository,
                inventoryAlertService
        );
    }

    @Test
    void getDashboard_shouldCalculateInventoryTotals() {

        mockEmptyCollections();

        when(inventoryStockQueryGateway.countProducts(store))
                .thenReturn(12L);

        when(inventoryStockQueryGateway.sumSimpleStock(store))
                .thenReturn(30L);

        when(inventoryStockQueryGateway.sumVariantStock(store))
                .thenReturn(20L);

        when(
                inventoryStockQueryGateway
                        .calculateSimpleInventoryValue(store)
        ).thenReturn(
                new BigDecimal("3000.00")
        );

        when(
                inventoryStockQueryGateway
                        .calculateVariantInventoryValue(store)
        ).thenReturn(
                new BigDecimal("2000.00")
        );

        InventoryDashboardDTO result =
                service.getDashboard(store);

        assertThat(result.totalProducts())
                .isEqualTo(12);

        assertThat(result.totalUnits())
                .isEqualTo(50);

        assertThat(result.estimatedInventoryValue())
                .isEqualByComparingTo("5000.00");

        assertThat(result.averageInventoryUnitValue())
                .isEqualByComparingTo("100.00");
    }

    @Test
    void getDashboard_shouldTreatNullInventoryValuesAsZero() {

        mockEmptyCollections();

        when(
                inventoryStockQueryGateway
                        .calculateSimpleInventoryValue(store)
        ).thenReturn(null);

        when(
                inventoryStockQueryGateway
                        .calculateVariantInventoryValue(store)
        ).thenReturn(null);

        InventoryDashboardDTO result =
                service.getDashboard(store);

        assertThat(result.estimatedInventoryValue())
                .isEqualByComparingTo("0");

        assertThat(result.averageInventoryUnitValue())
                .isEqualByComparingTo("0");
    }

    @Test
    void getDashboard_shouldSeparateOutOfStockFromLowStock() {

        InventoryProductStockDTO outOfStock =
                new InventoryProductStockDTO(
                        10L,
                        "Sin stock",
                        null,
                        null,
                        0,
                        5,
                        new BigDecimal("100.00"),
                        StockSeverity.OUT_OF_STOCK
                );

        InventoryProductStockDTO critical =
                new InventoryProductStockDTO(
                        11L,
                        "Crítico",
                        null,
                        null,
                        2,
                        5,
                        new BigDecimal("100.00"),
                        StockSeverity.CRITICAL
                );

        InventoryProductStockDTO low =
                new InventoryProductStockDTO(
                        12L,
                        "Bajo",
                        null,
                        null,
                        4,
                        5,
                        new BigDecimal("100.00"),
                        StockSeverity.LOW
                );

        mockEmptyMovementRepository();

        when(inventoryAlertService.getLowStockItems(store))
                .thenReturn(
                        List.of(
                                outOfStock,
                                critical,
                                low
                        )
                );

        InventoryDashboardDTO result =
                service.getDashboard(store);

        assertThat(result.outOfStockProducts())
                .isEqualTo(1);

        assertThat(result.lowStockProducts())
                .isEqualTo(2);

        assertThat(result.lowStockItems())
                .containsExactly(
                        outOfStock,
                        critical,
                        low
                );
    }

    @Test
    void getDashboard_shouldCalculateStockCoverageDays() {

        mockEmptyCollections();

        when(inventoryStockQueryGateway.sumSimpleStock(store))
                .thenReturn(60L);

        when(inventoryStockQueryGateway.sumVariantStock(store))
                .thenReturn(40L);

        when(
                movementRepository.sumSalesUnitsSince(
                        eq(store),
                        any(LocalDateTime.class)
                )
        ).thenReturn(300L);

        InventoryDashboardDTO result =
                service.getDashboard(store);

        assertThat(result.totalUnits())
                .isEqualTo(100);

        assertThat(result.salesUnitsLast30Days())
                .isEqualTo(300);

        /*
         * 300 / 30 = 10 unidades por día
         * 100 / 10 = 10 días
         */
        assertThat(result.estimatedStockCoverageDays())
                .isEqualByComparingTo("10.0");
    }

    @Test
    void getDashboard_shouldReturnZeroCoverageWhenNoSalesExist() {

        mockEmptyCollections();

        when(inventoryStockQueryGateway.sumSimpleStock(store))
                .thenReturn(100L);

        when(
                movementRepository.sumSalesUnitsSince(
                        eq(store),
                        any(LocalDateTime.class)
                )
        ).thenReturn(0L);

        InventoryDashboardDTO result =
                service.getDashboard(store);

        assertThat(result.totalUnits())
                .isEqualTo(100);

        assertThat(result.salesUnitsLast30Days())
                .isZero();

        assertThat(result.estimatedStockCoverageDays())
                .isEqualByComparingTo("0");
    }

    @Test
    void getDashboard_shouldExposeMovementMetricsAndRankings() {

        Producto producto = new Producto();
        producto.setId(10L);
        producto.setProductName("Producto");

        InventoryMovement movement =
                mock(InventoryMovement.class);

        when(movement.getId())
                .thenReturn(50L);

        when(movement.getCreatedAt())
                .thenReturn(
                        LocalDateTime.of(
                                2026,
                                9,
                                4,
                                12,
                                0
                        )
                );

        when(movement.getType())
                .thenReturn(
                        InventoryMovementType.SALE
                );

        when(movement.getProducto())
                .thenReturn(producto);

        when(movement.getVariante())
                .thenReturn(null);

        when(movement.getOrder())
                .thenReturn(null);

        when(movement.getQuantity())
                .thenReturn(3);

        when(movement.getStockBefore())
                .thenReturn(10);

        when(movement.getStockAfter())
                .thenReturn(7);

        when(movement.getReason())
                .thenReturn("Venta");

        InventoryTopMovementDTO top =
                new InventoryTopMovementDTO(
                        10L,
                        "Producto",
                        20L
                );

        InventoryTopMovementDTO slow =
                new InventoryTopMovementDTO(
                        20L,
                        "Producto lento",
                        1L
                );

        when(inventoryAlertService.getLowStockItems(store))
                .thenReturn(List.of());

        when(
                movementRepository.sumQuantityByTypesAndPeriod(
                        eq(store),
                        any(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                )
        )
                .thenReturn(5L)
                .thenReturn(3L);

        when(
                movementRepository.countMovementsByPeriod(
                        eq(store),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                )
        ).thenReturn(4L);

        when(
                movementRepository.sumSalesUnitsSince(
                        eq(store),
                        any(LocalDateTime.class)
                )
        ).thenReturn(30L);

        when(
                movementRepository
                        .findTop8ByStoreOrderByCreatedAtDesc(
                                store
                        )
        ).thenReturn(
                List.of(movement)
        );

        when(
                movementRepository.findTopOutgoingProducts(
                        eq(store),
                        any(),
                        any(LocalDateTime.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                List.of(top)
        );

        when(
                movementRepository.findSlowMovingProducts(
                        eq(store),
                        any(LocalDateTime.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                List.of(slow)
        );

        InventoryDashboardDTO result =
                service.getDashboard(store);

        assertThat(result.unitsInToday())
                .isEqualTo(5);

        assertThat(result.unitsOutToday())
                .isEqualTo(3);

        assertThat(result.movementsToday())
                .isEqualTo(4);

        assertThat(result.salesUnitsLast30Days())
                .isEqualTo(30);

        assertThat(result.recentMovements())
                .hasSize(1);

        assertThat(
                result.recentMovements()
                        .get(0)
                        .productName()
        ).isEqualTo("Producto");

        assertThat(result.topOutgoingProducts())
                .containsExactly(top);

        assertThat(result.slowMovingProducts())
                .containsExactly(slow);
    }

    private void mockEmptyCollections() {

        mockEmptyMovementRepository();

        when(
                inventoryAlertService
                        .getLowStockItems(store)
        ).thenReturn(List.of());
    }

    private void mockEmptyMovementRepository() {

        when(
                movementRepository.sumQuantityByTypesAndPeriod(
                        eq(store),
                        any(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                )
        ).thenReturn(0L);

        when(
                movementRepository.countMovementsByPeriod(
                        eq(store),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                )
        ).thenReturn(0L);

        /*
         * No configuramos sumSalesUnitsSince() aquí.
         *
         * Los tests que necesitan un valor concreto lo
         * configuran explícitamente. Para el resto,
         * Mockito devuelve 0L por defecto.
         */

        when(
                movementRepository
                        .findTop8ByStoreOrderByCreatedAtDesc(
                                store
                        )
        ).thenReturn(List.of());

        when(
                movementRepository.findTopOutgoingProducts(
                        eq(store),
                        any(),
                        any(LocalDateTime.class),
                        any(Pageable.class)
                )
        ).thenReturn(List.of());

        when(
                movementRepository.findSlowMovingProducts(
                        eq(store),
                        any(LocalDateTime.class),
                        any(Pageable.class)
                )
        ).thenReturn(List.of());
    }
}
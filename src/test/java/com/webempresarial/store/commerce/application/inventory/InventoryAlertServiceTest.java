package com.webempresarial.store.commerce.application.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.dto.inventory.InventoryProductStockDTO;
import com.webempresarial.store.dto.inventory.SimpleProductStockProjection;
import com.webempresarial.store.dto.inventory.StockSeverity;
import com.webempresarial.store.dto.inventory.VariantStockProjection;
import com.webempresarial.store.model.Store;

@ExtendWith(MockitoExtension.class)
class InventoryAlertServiceTest {

    @Mock
    private InventoryStockQueryGateway inventoryStockQueryGateway;

    private InventoryAlertService service;

    private Store store;

    @BeforeEach
    void setUp() {

        service = new InventoryAlertService(
                inventoryStockQueryGateway,
                5,
                2
        );

        store = new Store();
        store.setId(1L);
    }

    @Test
    void getLowStockItems_shouldRejectNullStore() {

        assertThatThrownBy(() ->
                service.getLowStockItems(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La tienda es obligatoria");

        verifyNoInteractions(inventoryStockQueryGateway);
    }

    @Test
    void getLowStockItems_shouldRejectStoreWithoutId() {

        Store invalidStore = new Store();

        assertThatThrownBy(() ->
                service.getLowStockItems(invalidStore)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La tienda es obligatoria");

        verifyNoInteractions(inventoryStockQueryGateway);
    }

    @Test
    void getLowStockItems_shouldCombineSimpleProductsAndVariants() {

        SimpleProductStockProjection simple =
                new SimpleProductStockProjection(
                        10L,
                        "Producto Simple",
                        4,
                        new BigDecimal("100.00")
                );

        VariantStockProjection variant =
                new VariantStockProjection(
                        20L,
                        "Producto Variante",
                        25L,
                        3,
                        new BigDecimal("150.00")
                );

        when(
                inventoryStockQueryGateway
                        .findLowStockSimpleProducts(
                                store,
                                5
                        )
        ).thenReturn(List.of(simple));

        when(
                inventoryStockQueryGateway
                        .findLowStockVariants(
                                store,
                                5
                        )
        ).thenReturn(List.of(variant));

        List<InventoryProductStockDTO> result =
                service.getLowStockItems(store);

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(
                        InventoryProductStockDTO::productId
                )
                .containsExactlyInAnyOrder(
                        10L,
                        20L
                );

        verify(inventoryStockQueryGateway)
                .findLowStockSimpleProducts(
                        store,
                        5
                );

        verify(inventoryStockQueryGateway)
                .findLowStockVariants(
                        store,
                        5
                );
    }

    @Test
    void getLowStockItems_shouldClassifyZeroStockAsOutOfStock() {

        when(
                inventoryStockQueryGateway
                        .findLowStockSimpleProducts(
                                store,
                                5
                        )
        ).thenReturn(
                List.of(
                        new SimpleProductStockProjection(
                                10L,
                                "Producto",
                                0,
                                new BigDecimal("100.00")
                        )
                )
        );

        when(
                inventoryStockQueryGateway
                        .findLowStockVariants(
                                store,
                                5
                        )
        ).thenReturn(List.of());

        InventoryProductStockDTO item =
                service.getLowStockItems(store)
                        .get(0);

        assertThat(item.severity())
                .isEqualTo(
                        StockSeverity.OUT_OF_STOCK
                );
    }

    @Test
    void getLowStockItems_shouldClassifyCriticalStock() {

        when(
                inventoryStockQueryGateway
                        .findLowStockSimpleProducts(
                                store,
                                5
                        )
        ).thenReturn(
                List.of(
                        new SimpleProductStockProjection(
                                10L,
                                "Producto",
                                2,
                                new BigDecimal("100.00")
                        )
                )
        );

        when(
                inventoryStockQueryGateway
                        .findLowStockVariants(
                                store,
                                5
                        )
        ).thenReturn(List.of());

        InventoryProductStockDTO item =
                service.getLowStockItems(store)
                        .get(0);

        assertThat(item.severity())
                .isEqualTo(
                        StockSeverity.CRITICAL
                );
    }

    @Test
    void getLowStockItems_shouldClassifyLowStock() {

        when(
                inventoryStockQueryGateway
                        .findLowStockSimpleProducts(
                                store,
                                5
                        )
        ).thenReturn(
                List.of(
                        new SimpleProductStockProjection(
                                10L,
                                "Producto",
                                4,
                                new BigDecimal("100.00")
                        )
                )
        );

        when(
                inventoryStockQueryGateway
                        .findLowStockVariants(
                                store,
                                5
                        )
        ).thenReturn(List.of());

        InventoryProductStockDTO item =
                service.getLowStockItems(store)
                        .get(0);

        assertThat(item.severity())
                .isEqualTo(
                        StockSeverity.LOW
                );
    }

    @Test
    void getLowStockItems_shouldSortByStockThenProductName() {

        when(
                inventoryStockQueryGateway
                        .findLowStockSimpleProducts(
                                store,
                                5
                        )
        ).thenReturn(
                List.of(
                        new SimpleProductStockProjection(
                                10L,
                                "Zeta",
                                2,
                                new BigDecimal("100.00")
                        ),
                        new SimpleProductStockProjection(
                                11L,
                                "Alpha",
                                2,
                                new BigDecimal("100.00")
                        ),
                        new SimpleProductStockProjection(
                                12L,
                                "Beta",
                                1,
                                new BigDecimal("100.00")
                        )
                )
        );

        when(
                inventoryStockQueryGateway
                        .findLowStockVariants(
                                store,
                                5
                        )
        ).thenReturn(List.of());

        List<InventoryProductStockDTO> result =
                service.getLowStockItems(store);

        assertThat(result)
                .extracting(
                        InventoryProductStockDTO::productName
                )
                .containsExactly(
                        "Beta",
                        "Alpha",
                        "Zeta"
                );
    }

    @Test
    void countOutOfStockItems_shouldCountOnlyOutOfStockItems() {

        when(
                inventoryStockQueryGateway
                        .findLowStockSimpleProducts(
                                store,
                                5
                        )
        ).thenReturn(
                List.of(
                        new SimpleProductStockProjection(
                                10L,
                                "Sin stock",
                                0,
                                new BigDecimal("100.00")
                        ),
                        new SimpleProductStockProjection(
                                11L,
                                "Crítico",
                                1,
                                new BigDecimal("100.00")
                        )
                )
        );

        when(
                inventoryStockQueryGateway
                        .findLowStockVariants(
                                store,
                                5
                        )
        ).thenReturn(List.of());

        assertThat(
                service.countOutOfStockItems(store)
        ).isEqualTo(1);
    }

    @Test
    void countCriticalStockItems_shouldCountOnlyCriticalItems() {

        when(
                inventoryStockQueryGateway
                        .findLowStockSimpleProducts(
                                store,
                                5
                        )
        ).thenReturn(
                List.of(
                        new SimpleProductStockProjection(
                                10L,
                                "Sin stock",
                                0,
                                new BigDecimal("100.00")
                        ),
                        new SimpleProductStockProjection(
                                11L,
                                "Crítico",
                                2,
                                new BigDecimal("100.00")
                        ),
                        new SimpleProductStockProjection(
                                12L,
                                "Bajo",
                                4,
                                new BigDecimal("100.00")
                        )
                )
        );

        when(
                inventoryStockQueryGateway
                        .findLowStockVariants(
                                store,
                                5
                        )
        ).thenReturn(List.of());

        assertThat(
                service.countCriticalStockItems(store)
        ).isEqualTo(1);
    }
}
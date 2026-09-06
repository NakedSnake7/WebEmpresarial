package com.webempresarial.store.commerce.infrastructure.inventory.scheduling;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.application.inventory.InventoryStockQueryGateway;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class InventoryAlertReconciliationJobTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private InventoryStockQueryGateway inventoryStockQueryGateway;

    @Mock
    private InventoryPersistentAlertService alertService;

    private InventoryAlertReconciliationJob job;

    @BeforeEach
    void setUp() {
        job = new InventoryAlertReconciliationJob(
                storeRepository,
                inventoryStockQueryGateway,
                alertService
        );
    }

    @Test
    void reconcile_shouldIgnoreInactiveStores() {

        Store activeStore = mock(Store.class);
        Store inactiveStore = mock(Store.class);

        when(activeStore.isActiva())
                .thenReturn(true);

        when(inactiveStore.isActiva())
                .thenReturn(false);

        when(storeRepository.findAll())
                .thenReturn(
                        List.of(
                                activeStore,
                                inactiveStore
                        )
                );

        when(
                inventoryStockQueryGateway
                        .findProductsWithVariantsAndStock(
                                activeStore
                        )
        ).thenReturn(List.of());

        job.reconcile();

        verify(inventoryStockQueryGateway)
                .findProductsWithVariantsAndStock(
                        activeStore
                );

        verify(inventoryStockQueryGateway, never())
                .findProductsWithVariantsAndStock(
                        inactiveStore
                );

        verifyNoInteractions(alertService);
    }

    @Test
    void reconcile_shouldEvaluateSimpleProduct() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(store.isActiva())
                .thenReturn(true);

        when(storeRepository.findAll())
                .thenReturn(List.of(store));

        when(
                inventoryStockQueryGateway
                        .findProductsWithVariantsAndStock(
                                store
                        )
        ).thenReturn(List.of(producto));

        when(producto.tieneVariantes())
                .thenReturn(false);

        job.reconcile();

        verify(alertService)
                .evaluateSimpleProduct(
                        producto,
                        store
                );

        verify(alertService, never())
                .evaluateVariant(
                        any(),
                        any()
                );
    }

    @Test
    void reconcile_shouldEvaluateEveryVariant() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        ProductoVariante first =
                mock(ProductoVariante.class);

        ProductoVariante second =
                mock(ProductoVariante.class);

        when(store.isActiva())
                .thenReturn(true);

        when(storeRepository.findAll())
                .thenReturn(List.of(store));

        when(
                inventoryStockQueryGateway
                        .findProductsWithVariantsAndStock(
                                store
                        )
        ).thenReturn(List.of(producto));

        when(producto.tieneVariantes())
                .thenReturn(true);

        when(producto.getVariantes())
                .thenReturn(
                        Set.of(
                                first,
                                second
                        )
                );

        job.reconcile();

        verify(alertService)
                .evaluateVariant(
                        first,
                        store
                );

        verify(alertService)
                .evaluateVariant(
                        second,
                        store
                );

        verify(alertService, never())
                .evaluateSimpleProduct(
                        any(),
                        any()
                );
    }
}
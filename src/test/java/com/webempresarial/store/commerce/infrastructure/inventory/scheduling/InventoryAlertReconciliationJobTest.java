package com.webempresarial.store.commerce.infrastructure.inventory.scheduling;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.application.inventory.InventoryStockQueryGateway;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class InventoryAlertReconciliationJobTest {

    @Mock StoreRepository storeRepository;
    @Mock InventoryStockQueryGateway inventoryStockQueryGateway;
    @Mock InventoryPersistentAlertService alertService;

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
        Store active = mock(Store.class);
        Store inactive = mock(Store.class);

        when(active.isActiva()).thenReturn(true);
        when(inactive.isActiva()).thenReturn(false);
        when(storeRepository.findAll())
                .thenReturn(List.of(active, inactive));

        when(inventoryStockQueryGateway
                .findProductsWithVariantsAndStock(active))
                .thenReturn(List.of());

        job.reconcile();

        verify(alertService).reconcileStore(
                active, List.of()
        );
        verify(inventoryStockQueryGateway, never())
                .findProductsWithVariantsAndStock(inactive);
        verifyNoMoreInteractions(alertService);
    }

    @Test
    void reconcile_shouldDelegateSimpleProducts() {
        Store store = mock(Store.class);
        Producto product = mock(Producto.class);

        when(store.isActiva()).thenReturn(true);
        when(storeRepository.findAll())
                .thenReturn(List.of(store));
        when(inventoryStockQueryGateway
                .findProductsWithVariantsAndStock(store))
                .thenReturn(List.of(product));

        job.reconcile();

        verify(alertService).reconcileStore(
                store, List.of(product)
        );
        verifyNoMoreInteractions(alertService);
    }

    @Test
    void reconcile_shouldDelegateVariantProducts() {
        Store store = mock(Store.class);
        Producto product = mock(Producto.class);

        when(store.isActiva()).thenReturn(true);
        when(storeRepository.findAll())
                .thenReturn(List.of(store));
        when(inventoryStockQueryGateway
                .findProductsWithVariantsAndStock(store))
                .thenReturn(List.of(product));

        job.reconcile();

        verify(alertService).reconcileStore(
                store, List.of(product)
        );
        verifyNoMoreInteractions(alertService);
    }
}

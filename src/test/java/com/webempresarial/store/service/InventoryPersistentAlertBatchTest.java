package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.domain.inventory.InventoryAlert;
import com.webempresarial.store.commerce.domain.inventory.InventoryAlertLevel;
import com.webempresarial.store.commerce.domain.inventory.InventoryAlertStatus;
import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryAlertRepository;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;

@ExtendWith(MockitoExtension.class)
class InventoryPersistentAlertBatchTest {

    @Mock InventoryAlertRepository repository;

    private InventoryPersistentAlertService service;
    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);
        service = new InventoryPersistentAlertService(
                repository, 5, 2
        );
    }

    private Producto simple(long id, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setStore(store);
        p.setStockSimple(stock);
        return p;
    }

    @Test
    void shouldSkipAlertQueryWhenStoreHasNoProducts() {
        service.reconcileStore(store, List.of());

        verifyNoInteractions(repository);
    }

    @Test
    void shouldLoadAlertsOnceForMultipleHealthyProducts() {
        Producto first = simple(100L, 20);
        Producto second = simple(101L, 30);

        service.reconcileStore(
                store, List.of(first, second)
        );

        verify(repository, times(1))
                .findByStoreAndStatusInOrderByLevelDescLastDetectedAtDesc(
                        eq(store), anyCollection()
                );
        verify(repository, never())
                .findByActiveKey(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldResolveExistingAlertWithOneBatchLookup() {
        Producto product = simple(10L, 8);
        InventoryAlert existing = new InventoryAlert();
        existing.setStore(store);
        existing.setProducto(product);
        existing.setStatus(InventoryAlertStatus.OPEN);
        existing.setLevel(InventoryAlertLevel.CRITICAL);
        existing.setCurrentStock(2);
        existing.setActiveKey(
                "INVENTORY:1:PRODUCT:10:VARIANT:SIMPLE"
        );

        when(repository
                .findByStoreAndStatusInOrderByLevelDescLastDetectedAtDesc(
                        eq(store), anyCollection()))
                .thenReturn(List.of(existing));

        service.reconcileStore(store, List.of(product));

        assertThat(existing.getStatus())
                .isEqualTo(InventoryAlertStatus.RESOLVED);
        assertThat(existing.getActiveKey()).isNull();
        verify(repository, never()).findByActiveKey(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldAvoidDuplicateInsertWithinSameBatch() {
        Producto product = simple(11L, 0);

        service.reconcileStore(
                store, List.of(product, product)
        );

        ArgumentCaptor<InventoryAlert> captor =
                ArgumentCaptor.forClass(InventoryAlert.class);

        verify(repository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getLevel())
                .isEqualTo(InventoryAlertLevel.OUT_OF_STOCK);
        verify(repository, never()).findByActiveKey(anyString());
    }

    @Test
    void shouldPreserveAcknowledgedAlertWhenUnchanged() {
        Producto product = simple(12L, 2);
        InventoryAlert existing = new InventoryAlert();
        existing.setStore(store);
        existing.setProducto(product);
        existing.setStatus(InventoryAlertStatus.ACKNOWLEDGED);
        existing.setLevel(InventoryAlertLevel.CRITICAL);
        existing.setCurrentStock(2);
        existing.setActiveKey(
                "INVENTORY:1:PRODUCT:12:VARIANT:SIMPLE"
        );

        when(repository
                .findByStoreAndStatusInOrderByLevelDescLastDetectedAtDesc(
                        eq(store), anyCollection()))
                .thenReturn(List.of(existing));

        service.reconcileStore(store, List.of(product));

        assertThat(existing.getStatus())
                .isEqualTo(InventoryAlertStatus.ACKNOWLEDGED);
        assertThat(existing.getOccurrenceCount()).isEqualTo(1);
        verify(repository, never()).findByActiveKey(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldEvaluateVariantWithoutIndividualLookup() {
        Producto parent = mock(Producto.class);
        ProductoVariante variant = mock(ProductoVariante.class);

        when(parent.getId()).thenReturn(20L);
        when(parent.getStore()).thenReturn(store);
        when(parent.tieneVariantes()).thenReturn(true);
        when(parent.getVariantes()).thenReturn(Set.of(variant));

        when(variant.getId()).thenReturn(30L);
        when(variant.getProducto()).thenReturn(parent);
        when(variant.getStock()).thenReturn(1);

        service.reconcileStore(store, List.of(parent));

        ArgumentCaptor<InventoryAlert> captor =
                ArgumentCaptor.forClass(InventoryAlert.class);

        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getActiveKey())
                .isEqualTo(
                        "INVENTORY:1:PRODUCT:20:VARIANT:30"
                );
        assertThat(captor.getValue().getLevel())
                .isEqualTo(InventoryAlertLevel.CRITICAL);

        verify(repository, never()).findByActiveKey(anyString());
    }
}

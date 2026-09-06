package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.*; 
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.domain.inventory.InventoryAlert;
import com.webempresarial.store.commerce.domain.inventory.InventoryAlertLevel;
import com.webempresarial.store.commerce.domain.inventory.InventoryAlertStatus;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryAlertRepository;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;

@ExtendWith(MockitoExtension.class)
class InventoryPersistentAlertServiceTest {

    private static final int LOW_THRESHOLD = 5;
    private static final int CRITICAL_THRESHOLD = 2;

    @Mock
    private InventoryAlertRepository repository;

    private InventoryPersistentAlertService service;

    private Store store;
    private Producto producto;

    @BeforeEach
    void setUp() {

        service = new InventoryPersistentAlertService(
                repository,
                LOW_THRESHOLD,
                CRITICAL_THRESHOLD
        );

        store = new Store();
        store.setId(1L);

        producto = new Producto();
        producto.setId(10L);
        producto.setStore(store);
    }

    @Test
    void shouldNotCreateAlertWhenSimpleProductHasHealthyStock() {

        producto.setStockSimple(6);

        when(repository.findByActiveKey(
                "INVENTORY:1:PRODUCT:10:VARIANT:SIMPLE"
        )).thenReturn(Optional.empty());

        service.evaluateSimpleProduct(
                producto,
                store
        );

        verify(repository, never())
                .save(any(InventoryAlert.class));
    }

    @Test
    void shouldCreateLowAlertWhenStockReachesLowThreshold() {

        producto.setStockSimple(5);

        when(repository.findByActiveKey(anyString()))
                .thenReturn(Optional.empty());

        service.evaluateSimpleProduct(
                producto,
                store
        );

        ArgumentCaptor<InventoryAlert> captor =
                ArgumentCaptor.forClass(
                        InventoryAlert.class
                );

        verify(repository).save(captor.capture());

        InventoryAlert alert = captor.getValue();

        assertThat(alert.getStore())
                .isSameAs(store);

        assertThat(alert.getProducto())
                .isSameAs(producto);

        assertThat(alert.getVariante())
                .isNull();

        assertThat(alert.getLevel())
                .isEqualTo(InventoryAlertLevel.LOW);

        assertThat(alert.getStatus())
                .isEqualTo(InventoryAlertStatus.OPEN);

        assertThat(alert.getCurrentStock())
                .isEqualTo(5);

        assertThat(alert.getStockThreshold())
                .isEqualTo(LOW_THRESHOLD);

        assertThat(alert.getActiveKey())
                .isEqualTo(
                        "INVENTORY:1:PRODUCT:10:VARIANT:SIMPLE"
                );
    }

    @Test
    void shouldCreateCriticalAlertWhenStockReachesCriticalThreshold() {

        producto.setStockSimple(2);

        when(repository.findByActiveKey(anyString()))
                .thenReturn(Optional.empty());

        service.evaluateSimpleProduct(
                producto,
                store
        );

        ArgumentCaptor<InventoryAlert> captor =
                ArgumentCaptor.forClass(
                        InventoryAlert.class
                );

        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getLevel())
                .isEqualTo(
                        InventoryAlertLevel.CRITICAL
                );

        assertThat(captor.getValue().getCurrentStock())
                .isEqualTo(2);
    }

    @Test
    void shouldCreateOutOfStockAlertWhenStockIsZero() {

        producto.setStockSimple(0);

        when(repository.findByActiveKey(anyString()))
                .thenReturn(Optional.empty());

        service.evaluateSimpleProduct(
                producto,
                store
        );

        ArgumentCaptor<InventoryAlert> captor =
                ArgumentCaptor.forClass(
                        InventoryAlert.class
                );

        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getLevel())
                .isEqualTo(
                        InventoryAlertLevel.OUT_OF_STOCK
                );

        assertThat(captor.getValue().getCurrentStock())
                .isZero();
    }

    @Test
    void shouldRefreshExistingAlertInsteadOfCreatingDuplicate() {

        producto.setStockSimple(2);

        InventoryAlert existing =
                activeAlert(
                        InventoryAlertLevel.LOW,
                        5,
                        null
                );

        when(repository.findByActiveKey(
                existing.getActiveKey()
        )).thenReturn(Optional.of(existing));

        service.evaluateSimpleProduct(
                producto,
                store
        );

        assertThat(existing.getLevel())
                .isEqualTo(
                        InventoryAlertLevel.CRITICAL
                );

        assertThat(existing.getCurrentStock())
                .isEqualTo(2);

        assertThat(existing.getOccurrenceCount())
                .isEqualTo(2);

        verify(repository, never())
                .save(any(InventoryAlert.class));
    }

    @Test
    void shouldResolveActiveAlertWhenStockBecomesHealthy() {

        producto.setStockSimple(7);

        InventoryAlert existing =
                activeAlert(
                        InventoryAlertLevel.CRITICAL,
                        2,
                        null
                );

        when(repository.findByActiveKey(
                existing.getActiveKey()
        )).thenReturn(Optional.of(existing));

        service.evaluateSimpleProduct(
                producto,
                store
        );

        assertThat(existing.getStatus())
                .isEqualTo(
                        InventoryAlertStatus.RESOLVED
                );

        assertThat(existing.getActiveKey())
                .isNull();

        assertThat(existing.getResolvedAt())
                .isNotNull();

        assertThat(existing.getResolutionNote())
                .contains("Stock actual: 7");

        verify(repository, never())
                .save(any(InventoryAlert.class));
    }

    @Test
    void shouldCreateIndependentAlertForVariant() {

        ProductoVariante variante =
                new ProductoVariante();

        variante.setId(25L);
        variante.setProducto(producto);
        variante.setStock(2);

        when(repository.findByActiveKey(
                "INVENTORY:1:PRODUCT:10:VARIANT:25"
        )).thenReturn(Optional.empty());

        service.evaluateVariant(
                variante,
                store
        );

        ArgumentCaptor<InventoryAlert> captor =
                ArgumentCaptor.forClass(
                        InventoryAlert.class
                );

        verify(repository).save(captor.capture());

        InventoryAlert alert = captor.getValue();

        assertThat(alert.getProducto())
                .isSameAs(producto);

        assertThat(alert.getVariante())
                .isSameAs(variante);

        assertThat(alert.getLevel())
                .isEqualTo(
                        InventoryAlertLevel.CRITICAL
                );

        assertThat(alert.getActiveKey())
                .isEqualTo(
                        "INVENTORY:1:PRODUCT:10:VARIANT:25"
                );
    }

    @Test
    void shouldRejectProductFromAnotherStore() {

        Store otherStore = new Store();
        otherStore.setId(99L);

        producto.setStockSimple(2);

        assertThatThrownBy(() ->
                service.evaluateSimpleProduct(
                        producto,
                        otherStore
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El producto no pertenece a la tienda"
                );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectVariantWithoutProduct() {

        ProductoVariante variante =
                new ProductoVariante();

        variante.setId(25L);
        variante.setStock(2);

        assertThatThrownBy(() ->
                service.evaluateVariant(
                        variante,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "La variante es obligatoria"
                );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldAcknowledgeActiveAlert() {

        InventoryAlert alert =
                activeAlert(
                        InventoryAlertLevel.LOW,
                        5,
                        null
                );

        when(repository.findByIdAndStore(
                100L,
                store
        )).thenReturn(Optional.of(alert));

        service.acknowledge(
                100L,
                store,
                "admin@webempresarial.com"
        );

        assertThat(alert.getStatus())
                .isEqualTo(
                        InventoryAlertStatus.ACKNOWLEDGED
                );

        assertThat(alert.getAcknowledgedBy())
                .isEqualTo(
                        "admin@webempresarial.com"
                );

        assertThat(alert.getAcknowledgedAt())
                .isNotNull();
    }

    @Test
    void shouldUseSystemWhenAcknowledgementUsernameIsBlank() {

        InventoryAlert alert =
                activeAlert(
                        InventoryAlertLevel.LOW,
                        5,
                        null
                );

        when(repository.findByIdAndStore(
                100L,
                store
        )).thenReturn(Optional.of(alert));

        service.acknowledge(
                100L,
                store,
                " "
        );

        assertThat(alert.getAcknowledgedBy())
                .isEqualTo("SYSTEM");
    }

    @Test
    void shouldCountOnlyActiveAlerts() {

        when(repository.countByStoreAndStatusIn(
                eq(store),
                anyCollection()
        )).thenReturn(3L);

        long result =
                service.countActive(store);

        assertThat(result).isEqualTo(3L);

        verify(repository)
                .countByStoreAndStatusIn(
                        eq(store),
                        argThat(statuses ->
                                statuses.contains(
                                        InventoryAlertStatus.OPEN
                                )
                                && statuses.contains(
                                        InventoryAlertStatus.ACKNOWLEDGED
                                )
                                && !statuses.contains(
                                        InventoryAlertStatus.RESOLVED
                                )
                        )
                );
    }

    @Test
    void shouldRejectInvalidThresholdConfiguration() {

        assertThatThrownBy(() ->
                new InventoryPersistentAlertService(
                        repository,
                        2,
                        5
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "límite crítico"
                );
    }

    private InventoryAlert activeAlert(
            InventoryAlertLevel level,
            int stock,
            ProductoVariante variante
    ) {

        InventoryAlert alert =
                new InventoryAlert();

        alert.setStore(store);
        alert.setProducto(producto);
        alert.setVariante(variante);
        alert.setLevel(level);
        alert.setStatus(
                InventoryAlertStatus.OPEN
        );
        alert.setCurrentStock(stock);
        alert.setStockThreshold(
                LOW_THRESHOLD
        );

        alert.setActiveKey(
                variante == null
                        ? "INVENTORY:1:PRODUCT:10:VARIANT:SIMPLE"
                        : "INVENTORY:1:PRODUCT:10:VARIANT:"
                            + variante.getId()
        );

        return alert;
    }
}
package com.webempresarial.store.commerce.application.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryMovementRepository;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

    @Mock
    private InventoryMovementRepository repository;

    private InventoryMovementService service;
    private Store store;

    @BeforeEach
    void setUp() {
        service =
                new InventoryMovementService(
                        repository
                );

        store = new Store();
        store.setId(1L);
    }

    @Test
    void hasMovementsForOrder_shouldReturnTrueWhenMovementsExist() {
        Long orderId = 100L;

        when(
                repository.existsByOrderIdAndStore(
                        orderId,
                        store
                )
        )
        .thenReturn(true);

        boolean result =
                service.hasMovementsForOrder(
                        orderId,
                        store
                );

        assertThat(result).isTrue();

        verify(repository)
                .existsByOrderIdAndStore(
                        orderId,
                        store
                );
    }

    @Test
    void hasMovementsForOrder_shouldReturnFalseWhenMovementsDoNotExist() {
        Long orderId = 100L;

        when(
                repository.existsByOrderIdAndStore(
                        orderId,
                        store
                )
        )
        .thenReturn(false);

        boolean result =
                service.hasMovementsForOrder(
                        orderId,
                        store
                );

        assertThat(result).isFalse();

        verify(repository)
                .existsByOrderIdAndStore(
                        orderId,
                        store
                );
    }

    @Test
    void hasMovementsForOrder_shouldRejectNullOrderId() {
        assertThatThrownBy(() ->
                service.hasMovementsForOrder(
                        null,
                        store
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("El orderId es obligatorio");

        verifyNoInteractions(repository);
    }

    @Test
    void hasMovementsForOrder_shouldRejectMissingStore() {
        assertThatThrownBy(() ->
                service.hasMovementsForOrder(
                        100L,
                        null
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("La tienda es obligatoria");

        verifyNoInteractions(repository);
    }

    @Test
    void recordShouldRejectProductFromAnotherStore() {
        Store otherStore = new Store();
        otherStore.setId(2L);

        Producto producto =
                mock(Producto.class);

        when(producto.getId())
                .thenReturn(10L);

        when(producto.getStore())
                .thenReturn(otherStore);

        assertThatThrownBy(() ->
                service.record(
                        store,
                        producto,
                        null,
                        null,
                        InventoryMovementType.ADJUSTMENT_IN,
                        1,
                        0,
                        1,
                        "Ajuste"
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "El producto no pertenece a la tienda"
        );

        verifyNoInteractions(repository);
    }

    @Test
    void recordShouldRejectVariantFromAnotherProduct() {
        Producto producto =
                mock(Producto.class);

        when(producto.getId())
                .thenReturn(10L);

        when(producto.getStore())
                .thenReturn(store);

        Producto otherProduct =
                mock(Producto.class);

        when(otherProduct.getId())
                .thenReturn(20L);

        ProductoVariante variante =
                mock(ProductoVariante.class);

        when(variante.getProducto())
                .thenReturn(otherProduct);

        assertThatThrownBy(() ->
                service.record(
                        store,
                        producto,
                        variante,
                        null,
                        InventoryMovementType.ADJUSTMENT_IN,
                        1,
                        0,
                        1,
                        "Ajuste"
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "La variante no pertenece al producto"
        );

        verifyNoInteractions(repository);
    }

    @Test
    void recordShouldRejectOrderFromAnotherStore() {
        Store otherStore = new Store();
        otherStore.setId(2L);

        Producto producto =
                mock(Producto.class);

        when(producto.getId())
                .thenReturn(10L);

        when(producto.getStore())
                .thenReturn(store);

        Order order =
                mock(Order.class);

        when(order.getStore())
                .thenReturn(otherStore);

        assertThatThrownBy(() ->
                service.record(
                        store,
                        producto,
                        null,
                        order,
                        InventoryMovementType.SALE,
                        1,
                        5,
                        4,
                        "Venta"
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "La orden no pertenece a la tienda"
        );

        verifyNoInteractions(repository);
    }
}

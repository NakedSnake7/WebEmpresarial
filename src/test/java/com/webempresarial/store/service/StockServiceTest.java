package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.*;  
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.exceptions.InsufficientStockException;
import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderItem;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.inventory.InventoryStockGateway;

import com.webempresarial.store.commerce.application.inventory.InventoryMovementService;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

	@Mock
	private InventoryStockGateway inventoryStockGateway;

    @Mock
    private InventoryMovementService inventoryMovementService;

    @Mock
    private InventoryPersistentAlertService persistentAlertService;

    private StockService service;

    private Store store;
    private Producto producto;

    @BeforeEach
    void setUp() {

    	service = new StockService(
    	        inventoryStockGateway,
    	        inventoryMovementService,
    	        persistentAlertService
    	);

        store = new Store();
        store.setId(1L);

        producto = new Producto();
        producto.setId(10L);
        producto.setStore(store);
        producto.setProductName("Producto Test");
        producto.setStockSimple(10);
    }

    @Test
    void shouldReduceSimpleProductStock() {

        Order order = orderWithSimpleItem(
                100L,
                producto,
                4
        );

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenReturn(producto);

        service.descontarStock(
                order,
                store
        );

        assertThat(producto.getStockSimple())
                .isEqualTo(6);

        assertThat(order.isStockReduced())
                .isTrue();

        verify(inventoryStockGateway)
        .saveProduct(producto);

        verify(inventoryMovementService)
                .record(
                        store,
                        producto,
                        null,
                        order,
                        InventoryMovementType.SALE,
                        4,
                        10,
                        6,
                        "Salida de inventario por orden #100"
                );

        verify(persistentAlertService)
                .evaluateSimpleProduct(
                        producto,
                        store
                );
    }

    @Test
    void shouldReduceVariantStock() {

        ProductoVariante variante =
                variant(25L, 8);

        Order order = orderWithVariantItem(
                100L,
                producto,
                variante,
                3
        );

        when(inventoryStockGateway.getVariantForUpdate(
                25L,
                store
        )).thenReturn(variante);

        service.descontarStock(
                order,
                store
        );

        assertThat(variante.getStock())
                .isEqualTo(5);

        assertThat(order.isStockReduced())
                .isTrue();

        verify(inventoryStockGateway)
        .saveVariant(variante);

        verify(inventoryMovementService)
                .record(
                        store,
                        producto,
                        variante,
                        order,
                        InventoryMovementType.SALE,
                        3,
                        8,
                        5,
                        "Salida de inventario por orden #100"
                );

        verify(persistentAlertService)
                .evaluateVariant(
                        variante,
                        store
                );
    }

    @Test
    void shouldRejectSimpleProductSaleWhenStockIsInsufficient() {

        producto.setStockSimple(2);

        Order order = orderWithSimpleItem(
                100L,
                producto,
                5
        );

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenReturn(producto);

        assertThatThrownBy(() ->
                service.descontarStock(
                        order,
                        store
                )
        )
                .isInstanceOf(
                        InsufficientStockException.class
                )
                .hasMessageContaining(
                        "Stock insuficiente"
                );

        assertThat(producto.getStockSimple())
                .isEqualTo(2);

        assertThat(order.isStockReduced())
                .isFalse();

        verify(inventoryStockGateway, never())
        .saveProduct(any());

        verifyNoInteractions(
                inventoryMovementService,
                persistentAlertService
        );
    }

    @Test
    void shouldRejectVariantSaleWhenStockIsInsufficient() {

        ProductoVariante variante =
                variant(25L, 1);

        Order order = orderWithVariantItem(
                100L,
                producto,
                variante,
                2
        );

        when(inventoryStockGateway.getVariantForUpdate(
                25L,
                store
        )).thenReturn(variante);

        assertThatThrownBy(() ->
                service.descontarStock(
                        order,
                        store
                )
        )
                .isInstanceOf(
                        InsufficientStockException.class
                );

        assertThat(variante.getStock())
                .isEqualTo(1);

        assertThat(order.isStockReduced())
                .isFalse();

        verify(inventoryStockGateway, never())
        .saveVariant(any());

        verifyNoInteractions(
                inventoryMovementService,
                persistentAlertService
        );
    }

    @Test
    void shouldNotReduceStockTwice() {

        Order order = orderWithSimpleItem(
                100L,
                producto,
                3
        );

        order.setStockReduced(true);

        service.descontarStock(
                order,
                store
        );

        assertThat(producto.getStockSimple())
                .isEqualTo(10);

        verifyNoInteractions(
                inventoryStockGateway,
                inventoryMovementService,
                persistentAlertService
        );
    }

    @Test
    void shouldRestoreSimpleProductStock() {

        producto.setStockSimple(6);

        Order order = orderWithSimpleItem(
                100L,
                producto,
                4
        );

        order.setStockReduced(true);

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenReturn(producto);

        service.restaurarStock(
                order,
                store
        );

        assertThat(producto.getStockSimple())
                .isEqualTo(10);

        assertThat(order.isStockReduced())
                .isFalse();

        verify(inventoryStockGateway)
        .saveProduct(producto);

        verify(inventoryMovementService)
                .record(
                        store,
                        producto,
                        null,
                        order,
                        InventoryMovementType.RESTORE,
                        4,
                        6,
                        10,
                        "Inventario restaurado por cancelación "
                                + "o expiración de la orden #100"
                );

        verify(persistentAlertService)
                .evaluateSimpleProduct(
                        producto,
                        store
                );
    }

    @Test
    void shouldRestoreVariantStock() {

        ProductoVariante variante =
                variant(25L, 5);

        Order order = orderWithVariantItem(
                100L,
                producto,
                variante,
                3
        );

        order.setStockReduced(true);

        when(inventoryStockGateway.getVariantForUpdate(
                25L,
                store
        )).thenReturn(variante);

        service.restaurarStock(
                order,
                store
        );

        assertThat(variante.getStock())
                .isEqualTo(8);

        assertThat(order.isStockReduced())
                .isFalse();

        verify(inventoryStockGateway)
        .saveVariant(variante);

        verify(inventoryMovementService)
                .record(
                        store,
                        producto,
                        variante,
                        order,
                        InventoryMovementType.RESTORE,
                        3,
                        5,
                        8,
                        "Inventario restaurado por cancelación "
                                + "o expiración de la orden #100"
                );

        verify(persistentAlertService)
                .evaluateVariant(
                        variante,
                        store
                );
    }

    @Test
    void shouldNotRestoreStockWhenItWasNeverReduced() {

        Order order = orderWithSimpleItem(
                100L,
                producto,
                3
        );

        order.setStockReduced(false);

        service.restaurarStock(
                order,
                store
        );

        assertThat(producto.getStockSimple())
                .isEqualTo(10);

        verifyNoInteractions(
                inventoryStockGateway,
                inventoryMovementService,
                persistentAlertService
        );
    }

    @Test
    void shouldRejectUnpersistedOrderWhenReducingStock() {

        Order order = new Order();

        assertThatThrownBy(() ->
                service.descontarStock(
                        order,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "orden debe estar persistida"
                );

        verifyNoInteractions(
                inventoryStockGateway,
                inventoryMovementService,
                persistentAlertService
        );
    }

    @Test
    void shouldRejectNullStore() {

        Order order = orderWithSimpleItem(
                100L,
                producto,
                1
        );

        assertThatThrownBy(() ->
                service.descontarStock(
                        order,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "La tienda es obligatoria"
                );

        verifyNoInteractions(
                inventoryStockGateway,
                inventoryMovementService,
                persistentAlertService
        );
    }

    @Test
    void shouldRejectOrderWithoutItems() {

        Order order = new Order();
        order.setId(100L);

        assertThatThrownBy(() ->
                service.descontarStock(
                        order,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "La orden no contiene productos"
                );
    }

    @Test
    void shouldFailWhenSimpleProductCannotBeLoaded() {

        Order order = orderWithSimpleItem(
                100L,
                producto,
                1
        );

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenThrow(
                new ResourceNotFoundException(
                        "Producto no encontrado: 10"
                )
        );

        assertThatThrownBy(() ->
        service.descontarStock(
                order,
                store
        )
)
        .isInstanceOf(
                ResourceNotFoundException.class
        )
        .hasMessage(
                "Producto no encontrado: 10"
        );
    }

    private Order orderWithSimpleItem(
            Long orderId,
            Producto producto,
            int quantity
    ) {
        OrderItem item = new OrderItem();

        item.setProducto(producto);
        item.setQuantity(quantity);

        Order order = new Order();

        order.setId(orderId);
        order.setItems(List.of(item));
        order.setStockReduced(false);

        return order;
    }

    private Order orderWithVariantItem(
            Long orderId,
            Producto producto,
            ProductoVariante variante,
            int quantity
    ) {
        OrderItem item = new OrderItem();

        item.setProducto(producto);
        item.setVariante(variante);
        item.setQuantity(quantity);

        Order order = new Order();

        order.setId(orderId);
        order.setItems(List.of(item));
        order.setStockReduced(false);

        return order;
    }

    private ProductoVariante variant(
            Long id,
            int stock
    ) {
        ProductoVariante variante =
                new ProductoVariante();

        variante.setId(id);
        variante.setProducto(producto);
        variante.setStock(stock);

        return variante;
    }
}
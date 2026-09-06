package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.*;  
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.dto.inventory.InventoryAdjustmentRequestDTO;
import com.webempresarial.store.exceptions.InsufficientStockException;
import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;


import com.webempresarial.store.commerce.application.inventory.InventoryAdjustmentService;
import com.webempresarial.store.commerce.application.inventory.InventoryMovementService;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.application.inventory.InventoryStockGateway;

@ExtendWith(MockitoExtension.class)
class InventoryAdjustmentServiceTest {

	@Mock
	private InventoryStockGateway inventoryStockGateway;

    @Mock
    private InventoryMovementService movementService;

    @Mock
    private InventoryPersistentAlertService persistentAlertService;

    private InventoryAdjustmentService service;

    private Store store;
    private Producto producto;

    @BeforeEach
    void setUp() {

    	service = new InventoryAdjustmentService(
    	        inventoryStockGateway,
    	        movementService,
    	        persistentAlertService
    	);

        store = new Store();
        store.setId(1L);

        producto = new Producto();
        producto.setId(10L);
        producto.setStore(store);
        producto.setStockSimple(10);
    }

    @Test
    void shouldIncreaseSimpleProductStock() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_IN,
                        5,
                        "Recepción de mercancía"
                );

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenReturn(producto);

        service.adjust(
                10L,
                request,
                store
        );

        assertThat(producto.getStockSimple())
                .isEqualTo(15);

        verify(inventoryStockGateway)
        .saveProduct(producto);

        verify(movementService)
                .record(
                        store,
                        producto,
                        null,
                        null,
                        InventoryMovementType.ADJUSTMENT_IN,
                        5,
                        10,
                        15,
                        "Recepción de mercancía"
                );

        verify(persistentAlertService)
                .evaluateSimpleProduct(
                        producto,
                        store
                );
    }

    @Test
    void shouldDecreaseSimpleProductStock() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_OUT,
                        4,
                        "Producto dañado"
                );

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenReturn(producto);

        service.adjust(
                10L,
                request,
                store
        );

        assertThat(producto.getStockSimple())
                .isEqualTo(6);

        verify(movementService)
                .record(
                        store,
                        producto,
                        null,
                        null,
                        InventoryMovementType.ADJUSTMENT_OUT,
                        4,
                        10,
                        6,
                        "Producto dañado"
                );

        verify(persistentAlertService)
                .evaluateSimpleProduct(
                        producto,
                        store
                );
    }

    @Test
    void shouldRejectSimpleProductOutputWhenStockIsInsufficient() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_OUT,
                        11,
                        "Ajuste físico"
                );

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenReturn(producto);

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        InsufficientStockException.class
                )
                .hasMessageContaining(
                        "No hay stock suficiente"
                );

        assertThat(producto.getStockSimple())
                .isEqualTo(10);

        verify(inventoryStockGateway, never())
        .saveProduct(any());

        verifyNoInteractions(
                movementService,
                persistentAlertService
        );
    }

    @Test
    void shouldIncreaseVariantStock() {

        ProductoVariante variante =
                variant(25L, 3);

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_IN,
                        7,
                        "Entrada variante"
                );

        request.setVariantId(25L);

        when(inventoryStockGateway.getVariantForUpdate(
                25L,
                store
        )).thenReturn(variante);
        
        service.adjust(
                10L,
                request,
                store
        );

        assertThat(variante.getStock())
                .isEqualTo(10);

        verify(inventoryStockGateway)
        .saveVariant(variante);

        verify(movementService)
                .record(
                        store,
                        producto,
                        variante,
                        null,
                        InventoryMovementType.ADJUSTMENT_IN,
                        7,
                        3,
                        10,
                        "Entrada variante"
                );

        verify(persistentAlertService)
                .evaluateVariant(
                        variante,
                        store
                );
    }

    @Test
    void shouldDecreaseVariantStock() {

        ProductoVariante variante =
                variant(25L, 8);

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_OUT,
                        3,
                        "Salida variante"
                );

        request.setVariantId(25L);

        when(inventoryStockGateway.getVariantForUpdate(
                25L,
                store
        )).thenReturn(variante);

        service.adjust(
                10L,
                request,
                store
        );

        assertThat(variante.getStock())
                .isEqualTo(5);

        verify(movementService)
                .record(
                        store,
                        producto,
                        variante,
                        null,
                        InventoryMovementType.ADJUSTMENT_OUT,
                        3,
                        8,
                        5,
                        "Salida variante"
                );

        verify(persistentAlertService)
                .evaluateVariant(
                        variante,
                        store
                );
    }

    @Test
    void shouldRejectVariantOutputWhenStockIsInsufficient() {

        ProductoVariante variante =
                variant(25L, 2);

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_OUT,
                        5,
                        "Salida inválida"
                );

        request.setVariantId(25L);

        when(inventoryStockGateway.getVariantForUpdate(
                25L,
                store
        )).thenReturn(variante);

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        InsufficientStockException.class
                );

        assertThat(variante.getStock())
                .isEqualTo(2);

        verify(inventoryStockGateway, never())
        .saveVariant(any());

        verifyNoInteractions(
                movementService,
                persistentAlertService
        );
    }

    @Test
    void shouldRejectVariantThatBelongsToDifferentProduct() {

        Producto anotherProduct =
                new Producto();

        anotherProduct.setId(99L);
        anotherProduct.setStore(store);

        ProductoVariante variante =
                new ProductoVariante();

        variante.setId(25L);
        variante.setProducto(anotherProduct);
        variante.setStock(5);

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_IN,
                        1,
                        "Entrada"
                );

        request.setVariantId(25L);

        when(inventoryStockGateway.getVariantForUpdate(
                25L,
                store
        )).thenReturn(variante);

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "La variante no pertenece al producto indicado"
                );

        verify(inventoryStockGateway, never())
        .saveVariant(any());

        verifyNoInteractions(
                movementService,
                persistentAlertService
        );
    }

    @Test
    void shouldFailWhenVariantDoesNotExist() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_IN,
                        1,
                        "Entrada"
                );

        request.setVariantId(25L);

        when(inventoryStockGateway.getVariantForUpdate(
                25L,
                store
        )).thenThrow(
                new ResourceNotFoundException(
                        "Variante no encontrada"
                )
        );

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Variante no encontrada"
                );
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_IN,
                        1,
                        "Entrada"
                );

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenThrow(
                new ResourceNotFoundException(
                        "Producto no encontrado"
                )
        );

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Producto no encontrado"
                );
    }

    @Test
    void shouldRejectAdjustmentWithoutRequest() {

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        null,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El ajuste es obligatorio"
                );

        verifyNoInteractions(
                inventoryStockGateway,
                movementService,
                persistentAlertService
        );
    }

    @Test
    void shouldRejectAdjustmentWithoutType() {

        InventoryAdjustmentRequestDTO request =
                new InventoryAdjustmentRequestDTO();

        request.setQuantity(1);
        request.setReason("Prueba");

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El tipo de ajuste es obligatorio"
                );
    }

    @Test
    void shouldRejectNonManualMovementType() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.SALE,
                        1,
                        "Intento incorrecto"
                );

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Solo se permiten ajustes de entrada o salida"
                );
    }

    @Test
    void shouldRejectZeroQuantity() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_IN,
                        0,
                        "Prueba"
                );

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "La cantidad debe ser mayor a cero"
                );
    }

    @Test
    void shouldRejectBlankReason() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_IN,
                        1,
                        " "
                );

        assertThatThrownBy(() ->
                service.adjust(
                        10L,
                        request,
                        store
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El motivo es obligatorio"
                );
    }

    @Test
    void shouldTrimReasonBeforeRecordingMovement() {

        InventoryAdjustmentRequestDTO request =
                request(
                        InventoryMovementType.ADJUSTMENT_IN,
                        1,
                        "   Recepción física   "
                );

        when(inventoryStockGateway.getProductForUpdate(
                10L,
                store
        )).thenReturn(producto);

        service.adjust(
                10L,
                request,
                store
        );

        verify(movementService)
                .record(
                        store,
                        producto,
                        null,
                        null,
                        InventoryMovementType.ADJUSTMENT_IN,
                        1,
                        10,
                        11,
                        "Recepción física"
                );
    }

    private InventoryAdjustmentRequestDTO request(
            InventoryMovementType type,
            int quantity,
            String reason
    ) {
        InventoryAdjustmentRequestDTO request =
                new InventoryAdjustmentRequestDTO();

        request.setType(type);
        request.setQuantity(quantity);
        request.setReason(reason);

        return request;
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
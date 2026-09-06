package com.webempresarial.store.commerce.application.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryMovementRepository;
import com.webempresarial.store.dto.inventory.InventoryProductDetailDTO;
import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;

@ExtendWith(MockitoExtension.class)
class InventoryProductDetailServiceTest {

    @Mock
    private InventoryStockQueryGateway inventoryStockQueryGateway;

    @Mock
    private InventoryMovementRepository movementRepository;

    private InventoryProductDetailService service;

    private Store store;

    @BeforeEach
    void setUp() {

        service = new InventoryProductDetailService(
                inventoryStockQueryGateway,
                movementRepository
        );

        store = new Store();
        store.setId(1L);
    }

    @Test
    void getDetail_shouldFailWhenProductDoesNotExist() {

        when(
                inventoryStockQueryGateway
                        .findProductWithDetails(
                                10L,
                                store
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getDetail(
                        10L,
                        store
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado");

        verifyNoInteractions(movementRepository);
    }

    @Test
    void getDetail_shouldReturnSimpleProductWithoutVariants() {

        Producto producto = new Producto();

        producto.setId(10L);
        producto.setProductName("Producto");
        producto.setSku("SKU-10");
        producto.setStockSimple(8);
        producto.setPrice(
                new BigDecimal("100.00")
        );

        when(
                inventoryStockQueryGateway
                        .findProductWithDetails(
                                10L,
                                store
                        )
        ).thenReturn(
                Optional.of(producto)
        );

        when(
                movementRepository
                        .findTop50ByProductoIdAndStoreOrderByCreatedAtDesc(
                                10L,
                                store
                        )
        ).thenReturn(List.of());

        InventoryProductDetailDTO result =
                service.getDetail(
                        10L,
                        store
                );

        assertThat(result.productId())
                .isEqualTo(10L);

        assertThat(result.productName())
                .isEqualTo("Producto");

        assertThat(result.sku())
                .isEqualTo("SKU-10");

        assertThat(result.hasVariants())
                .isFalse();

        assertThat(result.simpleStock())
                .isEqualTo(8);

        assertThat(result.basePrice())
                .isEqualByComparingTo("100.00");

        assertThat(result.variants())
                .isEmpty();

        assertThat(result.movements())
                .isEmpty();
    }

    @Test
    void getDetail_shouldMapVariants() {

        Producto producto = new Producto();

        producto.setId(10L);
        producto.setProductName("Producto");
        producto.setSku("SKU-10");
        producto.setStockSimple(0);
        producto.setPrice(
                new BigDecimal("100.00")
        );

        ProductoVariante variante =
                new ProductoVariante();

        variante.setId(20L);
        variante.setProducto(producto);
        variante.setStock(5);
        variante.setPrecio(
                new BigDecimal("120.00")
        );

        /*
         * Producto usa Set<ProductoVariante>.
         * Utilizamos el método de dominio para mantener
         * correctamente la relación producto-variante.
         */
        producto.agregarVariante(variante);

        when(
                inventoryStockQueryGateway
                        .findProductWithDetails(
                                10L,
                                store
                        )
        ).thenReturn(
                Optional.of(producto)
        );

        when(
                movementRepository
                        .findTop50ByProductoIdAndStoreOrderByCreatedAtDesc(
                                10L,
                                store
                        )
        ).thenReturn(List.of());

        InventoryProductDetailDTO result =
                service.getDetail(
                        10L,
                        store
                );

        assertThat(result.hasVariants())
                .isTrue();

        assertThat(result.variants())
                .hasSize(1);

        assertThat(
                result.variants()
                        .get(0)
                        .variantId()
        ).isEqualTo(20L);

        /*
         * No fijamos aquí el texto exacto de label porque
         * getNombreVisual() depende de los atributos de
         * ProductoVariante.
         *
         * Ese comportamiento pertenece a ProductoVariante,
         * no a InventoryProductDetailService.
         */

        assertThat(
                result.variants()
                        .get(0)
                        .stock()
        ).isEqualTo(5);

        assertThat(
                result.variants()
                        .get(0)
                        .price()
        ).isEqualByComparingTo("120.00");

        assertThat(result.movements())
                .isEmpty();
    }
}
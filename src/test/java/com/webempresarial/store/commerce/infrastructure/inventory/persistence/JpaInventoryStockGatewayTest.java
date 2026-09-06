package com.webempresarial.store.commerce.infrastructure.inventory.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;

@ExtendWith(MockitoExtension.class)
class JpaInventoryStockGatewayTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoVarianteRepository varianteRepository;

    private JpaInventoryStockGateway gateway;

    private Store store;

    @BeforeEach
    void setUp() {
        gateway = new JpaInventoryStockGateway(
                productoRepository,
                varianteRepository
        );

        store = new Store();
        store.setId(1L);
    }

    @Test
    void getProductForUpdate_shouldReturnStoreScopedProduct() {

        Producto producto = new Producto();

        when(productoRepository.findByIdForUpdate(10L, store))
                .thenReturn(Optional.of(producto));

        Producto result =
                gateway.getProductForUpdate(10L, store);

        assertThat(result).isSameAs(producto);

        verify(productoRepository)
                .findByIdForUpdate(10L, store);
    }

    @Test
    void getProductForUpdate_shouldFailWhenProductDoesNotExist() {

        when(productoRepository.findByIdForUpdate(10L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                gateway.getProductForUpdate(10L, store)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado: 10");
    }

    @Test
    void getVariantForUpdate_shouldReturnStoreScopedVariant() {

        ProductoVariante variante =
                new ProductoVariante();

        when(varianteRepository.findByIdForUpdate(20L, store))
                .thenReturn(Optional.of(variante));

        ProductoVariante result =
                gateway.getVariantForUpdate(20L, store);

        assertThat(result).isSameAs(variante);

        verify(varianteRepository)
                .findByIdForUpdate(20L, store);
    }

    @Test
    void getVariantForUpdate_shouldFailWhenVariantDoesNotExist() {

        when(varianteRepository.findByIdForUpdate(20L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                gateway.getVariantForUpdate(20L, store)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Variante no encontrada: 20");
    }

    @Test
    void saveProduct_shouldDelegateToRepository() {

        Producto producto = new Producto();

        when(productoRepository.save(producto))
                .thenReturn(producto);

        assertThat(
                gateway.saveProduct(producto)
        ).isSameAs(producto);

        verify(productoRepository)
                .save(producto);
    }

    @Test
    void saveVariant_shouldDelegateToRepository() {

        ProductoVariante variante =
                new ProductoVariante();

        when(varianteRepository.save(variante))
                .thenReturn(variante);

        assertThat(
                gateway.saveVariant(variante)
        ).isSameAs(variante);

        verify(varianteRepository)
                .save(variante);
    }
}
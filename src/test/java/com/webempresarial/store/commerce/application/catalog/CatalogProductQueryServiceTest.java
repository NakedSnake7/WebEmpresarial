package com.webempresarial.store.commerce.application.catalog;

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
class CatalogProductQueryServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoVarianteRepository productoVarianteRepository;

    private CatalogProductQueryService service;

    private Store store;

    @BeforeEach
    void setUp() {
        service = new CatalogProductQueryService(
                productoRepository,
                productoVarianteRepository
        );

        store = new Store();
        store.setId(1L);
    }

    @Test
    void obtenerProducto_shouldUseStoreScopedQuery() {

        Producto producto = new Producto();

        when(productoRepository.findByIdConTodo(100L, store))
                .thenReturn(Optional.of(producto));

        Producto result =
                service.obtenerProducto(100L, store);

        assertThat(result).isSameAs(producto);

        verify(productoRepository)
                .findByIdConTodo(100L, store);
    }

    @Test
    void obtenerProducto_shouldFailWhenProductDoesNotBelongToStore() {

        when(productoRepository.findByIdConTodo(100L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.obtenerProducto(100L, store)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Producto no encontrado: 100");
    }

    @Test
    void obtenerProductoConLock_shouldUseStoreScopedLock() {

        Producto producto = new Producto();

        when(productoRepository.findByIdForUpdate(100L, store))
                .thenReturn(Optional.of(producto));

        Producto result =
                service.obtenerProductoConLock(100L, store);

        assertThat(result).isSameAs(producto);

        verify(productoRepository)
                .findByIdForUpdate(100L, store);
    }

    @Test
    void obtenerProductoConLock_shouldFailWhenProductDoesNotBelongToStore() {

        when(productoRepository.findByIdForUpdate(100L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.obtenerProductoConLock(100L, store)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado: 100");
    }

    @Test
    void obtenerVarianteConLock_shouldUseStoreScopedLock() {

        ProductoVariante variante =
                new ProductoVariante();

        when(
                productoVarianteRepository
                        .findByIdForUpdate(10L, store)
        ).thenReturn(Optional.of(variante));

        ProductoVariante result =
                service.obtenerVarianteConLock(10L, store);

        assertThat(result).isSameAs(variante);

        verify(productoVarianteRepository)
                .findByIdForUpdate(10L, store);
    }

    @Test
    void obtenerVarianteConLock_shouldFailWhenVariantDoesNotBelongToStore() {

        when(
                productoVarianteRepository
                        .findByIdForUpdate(10L, store)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.obtenerVarianteConLock(10L, store)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Variante no encontrada: 10");
    }
}
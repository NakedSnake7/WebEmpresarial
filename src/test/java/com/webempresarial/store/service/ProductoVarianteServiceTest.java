package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoVarianteRepository;

@ExtendWith(MockitoExtension.class)
class ProductoVarianteServiceTest {

    @Mock
    private ProductoVarianteRepository repository;

    private ProductoVarianteService service;

    @BeforeEach
    void setUp() {
        service = new ProductoVarianteService(repository);
    }

    @Test
    void actualizarPrecio_shouldUpdateVariantBelongingToStore() {

        Store store = mock(Store.class);
        ProductoVariante variante = mock(ProductoVariante.class);

        when(repository.findByIdAndStore(20L, store))
                .thenReturn(Optional.of(variante));

        service.actualizarPrecio(
                20L,
                new BigDecimal("125.50"),
                store
        );

        verify(repository)
                .findByIdAndStore(20L, store);

        verify(variante)
                .setPrecio(new BigDecimal("125.50"));

        verify(repository, never())
                .findById(anyLong());
    }

    @Test
    void actualizarPrecio_shouldFailWhenVariantDoesNotBelongToStore() {

        Store store = mock(Store.class);

        when(repository.findByIdAndStore(20L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.actualizarPrecio(
                        20L,
                        new BigDecimal("125.50"),
                        store
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Variante no encontrada");

        verify(repository)
                .findByIdAndStore(20L, store);

        verify(repository, never())
                .findById(anyLong());
    }
}
package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.model.Marca;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.MarcaRepository;

@ExtendWith(MockitoExtension.class)
class MarcaServiceTest {

    @Mock
    private MarcaRepository repository;

    private MarcaService service;

    @BeforeEach
    void setUp() {
        service = new MarcaService(repository);
    }

    @Test
    void obtenerTodas_shouldUseCurrentStore() {

        Store store = mock(Store.class);
        List<Marca> marcas =
                List.of(mock(Marca.class));

        when(repository.findByStoreOrderByNombreAsc(store))
                .thenReturn(marcas);

        assertThat(service.obtenerTodas(store))
                .isSameAs(marcas);
    }

    @Test
    void obtenerPorId_shouldReturnBrandBelongingToStore() {

        Store store = mock(Store.class);
        Marca marca = mock(Marca.class);

        when(repository.findByIdAndStore(20L, store))
                .thenReturn(Optional.of(marca));

        assertThat(service.obtenerPorId(20L, store))
                .isSameAs(marca);
    }

    @Test
    void obtenerPorId_shouldFailWhenBrandDoesNotBelongToStore() {

        Store store = mock(Store.class);

        when(repository.findByIdAndStore(20L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.obtenerPorId(20L, store)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage(
                        "Marca no encontrada ID: 20"
                );
    }

    @Test
    void obtenerPorId_shouldReturnNullForNullId() {

        Store store = mock(Store.class);

        assertThat(service.obtenerPorId(null, store))
                .isNull();

        verifyNoInteractions(repository);
    }

    @Test
    void obtenerOCrear_shouldReturnExistingBrand() {

        Store store = mock(Store.class);
        Marca existente = mock(Marca.class);

        when(
                repository.findByNombreIgnoreCaseAndStore(
                        "Acme",
                        store
                )
        ).thenReturn(Optional.of(existente));

        Marca result =
                service.obtenerOCrear(
                        "  Acme  ",
                        store
                );

        assertThat(result)
                .isSameAs(existente);

        verify(repository, never())
                .save(any());
    }

    @Test
    void obtenerOCrear_shouldCreateBrandForCurrentStore() {

        Store store = mock(Store.class);

        when(
                repository.findByNombreIgnoreCaseAndStore(
                        "Acme",
                        store
                )
        ).thenReturn(Optional.empty());

        when(repository.save(any(Marca.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Marca result =
                service.obtenerOCrear(
                        "  Acme  ",
                        store
                );

        assertThat(result.getNombre())
                .isEqualTo("Acme");

        assertThat(result.getStore())
                .isSameAs(store);

        verify(repository)
                .save(result);
    }

    @Test
    void obtenerOCrear_shouldReturnNullForBlankName() {

        Store store = mock(Store.class);

        assertThat(
                service.obtenerOCrear("   ", store)
        ).isNull();

        verifyNoInteractions(repository);
    }

    @Test
    void eliminar_shouldResolveBrandWithinStoreBeforeDeleting() {

        Store store = mock(Store.class);
        Marca marca = mock(Marca.class);

        when(repository.findByIdAndStore(20L, store))
                .thenReturn(Optional.of(marca));

        service.eliminar(20L, store);

        verify(repository)
                .findByIdAndStore(20L, store);

        verify(repository)
                .delete(marca);
    }

    @Test
    void eliminar_shouldDoNothingForNullId() {

        Store store = mock(Store.class);

        service.eliminar(null, store);

        verifyNoInteractions(repository);
    }
}
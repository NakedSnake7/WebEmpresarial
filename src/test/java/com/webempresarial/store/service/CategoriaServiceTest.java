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

import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    private CategoriaService service;

    @BeforeEach
    void setUp() {
        service = new CategoriaService(repository);
    }

    @Test
    void obtenerTodas_shouldUseCurrentStore() {

        Store store = mock(Store.class);
        List<Categoria> categorias =
                List.of(mock(Categoria.class));

        when(repository.findByStoreOrderByNombreAsc(store))
                .thenReturn(categorias);

        assertThat(service.obtenerTodas(store))
                .isSameAs(categorias);

        verify(repository)
                .findByStoreOrderByNombreAsc(store);
    }

    @Test
    void obtenerPorId_shouldReturnCategoryBelongingToStore() {

        Store store = mock(Store.class);
        Categoria categoria = mock(Categoria.class);

        when(repository.findByIdAndStore(10L, store))
                .thenReturn(Optional.of(categoria));

        assertThat(service.obtenerPorId(10L, store))
                .isSameAs(categoria);

        verify(repository)
                .findByIdAndStore(10L, store);
    }

    @Test
    void obtenerPorId_shouldFailWhenCategoryDoesNotBelongToStore() {

        Store store = mock(Store.class);

        when(repository.findByIdAndStore(10L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.obtenerPorId(10L, store)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage(
                        "Categoría no encontrada con ID: 10"
                );
    }

    @Test
    void guardar_shouldAssignStoreBeforeSaving() {

        Store store = mock(Store.class);
        Categoria categoria = new Categoria();

        when(repository.save(categoria))
                .thenReturn(categoria);

        Categoria result =
                service.guardar(categoria, store);

        assertThat(result)
                .isSameAs(categoria);

        assertThat(categoria.getStore())
                .isSameAs(store);

        verify(repository)
                .save(categoria);
    }

    @Test
    void eliminar_shouldResolveCategoryWithinStoreBeforeDeleting() {

        Store store = mock(Store.class);
        Categoria categoria = mock(Categoria.class);

        when(repository.findByIdAndStore(10L, store))
                .thenReturn(Optional.of(categoria));

        service.eliminar(10L, store);

        verify(repository)
                .findByIdAndStore(10L, store);

        verify(repository)
                .delete(categoria);
    }

    @Test
    void obtenerOCrearCategoria_shouldReturnExistingCategory() {

        Store store = mock(Store.class);
        Categoria existente = mock(Categoria.class);

        when(
                repository.findByNombreIgnoreCaseAndStore(
                        "Bebidas",
                        store
                )
        ).thenReturn(Optional.of(existente));

        Categoria result =
                service.obtenerOCrearCategoria(
                        "  Bebidas  ",
                        store
                );

        assertThat(result)
                .isSameAs(existente);

        verify(repository, never())
                .save(any());
    }

    @Test
    void obtenerOCrearCategoria_shouldCreateCategoryForCurrentStore() {

        Store store = mock(Store.class);

        when(
                repository.findByNombreIgnoreCaseAndStore(
                        "Bebidas",
                        store
                )
        ).thenReturn(Optional.empty());

        when(repository.save(any(Categoria.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Categoria result =
                service.obtenerOCrearCategoria(
                        "  Bebidas  ",
                        store
                );

        assertThat(result.getNombre())
                .isEqualTo("Bebidas");

        assertThat(result.getStore())
                .isSameAs(store);

        verify(repository)
                .save(result);
    }

    @Test
    void obtenerOCrearCategoria_shouldRejectBlankName() {

        Store store = mock(Store.class);

        assertThatThrownBy(() ->
                service.obtenerOCrearCategoria(
                        "   ",
                        store
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage(
                        "La categoría no puede ser nula o vacía"
                );

        verifyNoInteractions(repository);
    }
}
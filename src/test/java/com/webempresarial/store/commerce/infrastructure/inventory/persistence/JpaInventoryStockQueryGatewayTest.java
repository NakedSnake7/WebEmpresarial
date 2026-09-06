package com.webempresarial.store.commerce.infrastructure.inventory.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.dto.inventory.SimpleProductStockProjection;
import com.webempresarial.store.dto.inventory.VariantStockProjection;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;

@ExtendWith(MockitoExtension.class)
class JpaInventoryStockQueryGatewayTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoVarianteRepository varianteRepository;

    private JpaInventoryStockQueryGateway gateway;

    private Store store;

    @BeforeEach
    void setUp() {
        gateway = new JpaInventoryStockQueryGateway(
                productoRepository,
                varianteRepository
        );

        store = new Store();
        store.setId(1L);
    }

    @Test
    void findProductWithDetails_shouldDelegateToStoreScopedRepositoryQuery() {

        Producto producto = new Producto();

        when(
                productoRepository.findByIdConTodo(
                        10L,
                        store
                )
        ).thenReturn(Optional.of(producto));

        Optional<Producto> result =
                gateway.findProductWithDetails(
                        10L,
                        store
                );

        assertThat(result)
                .containsSame(producto);

        verify(productoRepository)
                .findByIdConTodo(
                        10L,
                        store
                );
    }

    @Test
    void findProductWithDetails_shouldReturnEmptyWhenProductDoesNotExist() {

        when(
                productoRepository.findByIdConTodo(
                        10L,
                        store
                )
        ).thenReturn(Optional.empty());

        assertThat(
                gateway.findProductWithDetails(
                        10L,
                        store
                )
        ).isEmpty();
    }

    @Test
    void findLowStockSimpleProducts_shouldDelegate() {

        SimpleProductStockProjection projection =
                new SimpleProductStockProjection(
                        10L,
                        "Producto",
                        3,
                        new BigDecimal("100.00")
                );

        when(
                productoRepository.findLowStockSimpleProducts(
                        store,
                        5
                )
        ).thenReturn(List.of(projection));

        assertThat(
                gateway.findLowStockSimpleProducts(
                        store,
                        5
                )
        ).containsExactly(projection);
    }

    @Test
    void findLowStockVariants_shouldDelegate() {

        VariantStockProjection projection =
                new VariantStockProjection(
                        10L,
                        "Producto",
                        20L,
                        2,
                        new BigDecimal("150.00")
                );

        when(
                varianteRepository.findLowStockVariants(
                        store,
                        5
                )
        ).thenReturn(List.of(projection));

        assertThat(
                gateway.findLowStockVariants(
                        store,
                        5
                )
        ).containsExactly(projection);
    }

    @Test
    void countProducts_shouldUseStoreId() {

        when(
                productoRepository.countByStoreId(1L)
        ).thenReturn(12L);

        assertThat(
                gateway.countProducts(store)
        ).isEqualTo(12L);

        verify(productoRepository)
                .countByStoreId(1L);
    }

    @Test
    void stockAndValueQueries_shouldDelegate() {

        when(productoRepository.sumSimpleStock(store))
                .thenReturn(30L);

        when(varianteRepository.sumVariantStock(store))
                .thenReturn(20L);

        when(
                productoRepository
                        .calculateSimpleInventoryValue(store)
        ).thenReturn(
                new BigDecimal("3000.00")
        );

        when(
                varianteRepository
                        .calculateVariantInventoryValue(store)
        ).thenReturn(
                new BigDecimal("2000.00")
        );

        assertThat(gateway.sumSimpleStock(store))
                .isEqualTo(30L);

        assertThat(gateway.sumVariantStock(store))
                .isEqualTo(20L);

        assertThat(
                gateway.calculateSimpleInventoryValue(store)
        ).isEqualByComparingTo("3000.00");

        assertThat(
                gateway.calculateVariantInventoryValue(store)
        ).isEqualByComparingTo("2000.00");
    }

    @Test
    void findProductsWithVariantsAndStock_shouldDelegate() {

        Producto producto = new Producto();

        when(
                productoRepository
                        .findProductosConVariantesYStock(store)
        ).thenReturn(List.of(producto));

        assertThat(
                gateway.findProductsWithVariantsAndStock(store)
        ).containsExactly(producto);
    }
}
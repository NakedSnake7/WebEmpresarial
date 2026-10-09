package com.webempresarial.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.dto.producto.publico.ProductoCardDTO;
import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;

@SpringBootTest
@Transactional
class ProductoRepositoryStorefrontProjectionIntegrationTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Test
    void findProductosIndexOptimizado_shouldExposeAggregatedVariantStock() {

        Store store = createStore(
                "Storefront Projection Store",
                "storefront-projection.local"
        );

        Categoria categoria = createCategory(
                store,
                "Storefront Projection Category"
        );

        Producto producto = createProduct(
                store,
                categoria,
                "Variant Stock Product"
        );

        createVariant(producto, 4, new BigDecimal("100.00"));
        createVariant(producto, 6, new BigDecimal("120.00"));

        List<ProductoCardDTO> products =
                productoRepository.findProductosIndexOptimizado(store);

        assertThat(products).hasSize(1);

        ProductoCardDTO dto = products.get(0);

        assertThat(dto.isTieneVariantes()).isTrue();
        assertThat(dto.getStockSimple()).isEqualTo(10);
    }

    private Store createStore(
            String name,
            String domain
    ) {

        Store store = new Store();

        store.setNombre(name);
        store.setDominio(domain);
        store.setActiva(true);

        return storeRepository.saveAndFlush(store);
    }

    private Categoria createCategory(
            Store store,
            String name
    ) {

        Categoria categoria = new Categoria();

        categoria.setNombre(name);
        categoria.setStore(store);

        return categoriaRepository.saveAndFlush(categoria);
    }

    private Producto createProduct(
            Store store,
            Categoria categoria,
            String name
    ) {

        Producto producto = new Producto();

        producto.setStore(store);
        producto.setProductName(name);
        producto.setPrice(new BigDecimal("100.00"));
        producto.setStockSimple(0);
        producto.setCategoria(categoria);
        producto.setVisibleEnMenu(true);
        producto.setTienePromocion(false);
        producto.setPorcentajeDescuento(0.0);

        return productoRepository.saveAndFlush(producto);
    }

    private ProductoVariante createVariant(
            Producto producto,
            int stock,
            BigDecimal price
    ) {

        ProductoVariante variante = new ProductoVariante();

        variante.setProducto(producto);
        variante.setStock(stock);
        variante.setPrecio(price);
        variante.setPrincipal(false);

        return varianteRepository.saveAndFlush(variante);
    }
    @Test
    void findProductosIndexOptimizado_shouldPreserveSimpleProductStock() {

        Store store = createStore(
                "Simple Stock Store",
                "simple-stock.local"
        );

        Categoria categoria = createCategory(
                store,
                "Simple Stock Category"
        );

        Producto producto = createProduct(
                store,
                categoria,
                "Simple Stock Product"
        );

        producto.setStockSimple(7);
        productoRepository.saveAndFlush(producto);

        List<ProductoCardDTO> products =
                productoRepository.findProductosIndexOptimizado(store);

        assertThat(products).hasSize(1);

        ProductoCardDTO dto = products.get(0);

        assertThat(dto.isTieneVariantes()).isFalse();
        assertThat(dto.getStockSimple()).isEqualTo(7);
    }
}

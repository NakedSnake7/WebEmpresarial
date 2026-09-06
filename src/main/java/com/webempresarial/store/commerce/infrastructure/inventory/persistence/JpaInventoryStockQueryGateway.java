package com.webempresarial.store.commerce.infrastructure.inventory.persistence;

import java.math.BigDecimal; 
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.webempresarial.store.commerce.application.inventory.InventoryStockQueryGateway;
import com.webempresarial.store.dto.inventory.SimpleProductStockProjection;
import com.webempresarial.store.dto.inventory.VariantStockProjection;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;

@Component
public class JpaInventoryStockQueryGateway
        implements InventoryStockQueryGateway {

    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository varianteRepository;

    public JpaInventoryStockQueryGateway(
            ProductoRepository productoRepository,
            ProductoVarianteRepository varianteRepository
    ) {
        this.productoRepository = productoRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    public Optional<Producto> findProductWithDetails(
            Long productId,
            Store store
    ) {
        return productoRepository
                .findByIdConTodo(
                        productId,
                        store
                );
    }
    
    @Override
    public List<SimpleProductStockProjection> findLowStockSimpleProducts(
            Store store,
            int threshold
    ) {
        return productoRepository
                .findLowStockSimpleProducts(
                        store,
                        threshold
                );
    }

    @Override
    public List<VariantStockProjection> findLowStockVariants(
            Store store,
            int threshold
    ) {
        return varianteRepository
                .findLowStockVariants(
                        store,
                        threshold
                );
    }

    @Override
    public long countProducts(
            Store store
    ) {
        return productoRepository
                .countByStoreId(
                        store.getId()
                );
    }

    @Override
    public long sumSimpleStock(
            Store store
    ) {
        return productoRepository
                .sumSimpleStock(store);
    }

    @Override
    public long sumVariantStock(
            Store store
    ) {
        return varianteRepository
                .sumVariantStock(store);
    }

    @Override
    public BigDecimal calculateSimpleInventoryValue(
            Store store
    ) {
        return productoRepository
                .calculateSimpleInventoryValue(store);
    }

    @Override
    public BigDecimal calculateVariantInventoryValue(
            Store store
    ) {
        return varianteRepository
                .calculateVariantInventoryValue(store);
    }

    @Override
    public List<Producto> findProductsWithVariantsAndStock(
            Store store
    ) {
        return productoRepository
                .findProductosConVariantesYStock(store);
    }
}
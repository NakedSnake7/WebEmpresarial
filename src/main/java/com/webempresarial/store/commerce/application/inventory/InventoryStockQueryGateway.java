package com.webempresarial.store.commerce.application.inventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.webempresarial.store.dto.inventory.SimpleProductStockProjection;
import com.webempresarial.store.dto.inventory.VariantStockProjection;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;

public interface InventoryStockQueryGateway {

    List<SimpleProductStockProjection> findLowStockSimpleProducts(
            Store store,
            int threshold
    );

    List<VariantStockProjection> findLowStockVariants(
            Store store,
            int threshold
    );

    long countProducts(
            Store store
    );

    long sumSimpleStock(
            Store store
    );

    long sumVariantStock(
            Store store
    );

    BigDecimal calculateSimpleInventoryValue(
            Store store
    );

    BigDecimal calculateVariantInventoryValue(
            Store store
    );

    List<Producto> findProductsWithVariantsAndStock(
            Store store
    );

    Optional<Producto> findProductWithDetails(
            Long productId,
            Store store
    );
}
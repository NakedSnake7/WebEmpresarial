package com.webempresarial.store.commerce.application.inventory;

import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;

public interface InventoryStockGateway {

    Producto getProductForUpdate(
            Long productId,
            Store store
    );

    ProductoVariante getVariantForUpdate(
            Long variantId,
            Store store
    );

    Producto saveProduct(
            Producto producto
    );

    ProductoVariante saveVariant(
            ProductoVariante variante
    );
}
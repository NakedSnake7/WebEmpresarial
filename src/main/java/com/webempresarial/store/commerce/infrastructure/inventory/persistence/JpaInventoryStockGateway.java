package com.webempresarial.store.commerce.infrastructure.inventory.persistence;

import org.springframework.stereotype.Component;

import com.webempresarial.store.commerce.application.inventory.InventoryStockGateway;
import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;

@Component
public class JpaInventoryStockGateway
        implements InventoryStockGateway {

    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository varianteRepository;

    public JpaInventoryStockGateway(
            ProductoRepository productoRepository,
            ProductoVarianteRepository varianteRepository
    ) {
        this.productoRepository = productoRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    public Producto getProductForUpdate(
            Long productId,
            Store store
    ) {
        return productoRepository
                .findByIdForUpdate(productId, store)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Producto no encontrado: " + productId
                        )
                );
    }

    @Override
    public ProductoVariante getVariantForUpdate(
            Long variantId,
            Store store
    ) {
        return varianteRepository
                .findByIdForUpdate(variantId, store)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Variante no encontrada: " + variantId
                        )
                );
    }

    @Override
    public Producto saveProduct(
            Producto producto
    ) {
        return productoRepository.save(producto);
    }

    @Override
    public ProductoVariante saveVariant(
            ProductoVariante variante
    ) {
        return varianteRepository.save(variante);
    }
}
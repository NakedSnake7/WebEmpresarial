package com.webempresarial.store.commerce.application.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;

@Service
public class CatalogProductQueryService {

    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository productoVarianteRepository;

    public CatalogProductQueryService(
            ProductoRepository productoRepository,
            ProductoVarianteRepository productoVarianteRepository
    ) {
        this.productoRepository = productoRepository;
        this.productoVarianteRepository = productoVarianteRepository;
    }

    public Producto obtenerProducto(
            Long productId,
            Store store
    ) {
        return productoRepository
                .findByIdConTodo(productId, store)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Producto no encontrado: " + productId
                        )
                );
    }

    @Transactional
    public Producto obtenerProductoConLock(
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

    @Transactional
    public ProductoVariante obtenerVarianteConLock(
            Long varianteId,
            Store store
    ) {
        return productoVarianteRepository
                .findByIdForUpdate(varianteId, store)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Variante no encontrada: " + varianteId
                        )
                );
    }
}
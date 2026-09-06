package com.webempresarial.store.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoVarianteRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductoVarianteService {

    private final ProductoVarianteRepository repository;

    public ProductoVarianteService(
            ProductoVarianteRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public void actualizarPrecio(
            Long id,
            BigDecimal precio,
            Store store
    ) {

        ProductoVariante variante =
                repository.findByIdAndStore(id, store)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Variante no encontrada"
                                )
                        );

        variante.setPrecio(precio);
    }
}
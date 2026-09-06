package com.webempresarial.store.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.ProductoVarianteService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/variantes")
public class VarianteController {

    private final ProductoVarianteService varianteService;
    private final StoreResolver storeResolver;

    public VarianteController(
            ProductoVarianteService varianteService,
            StoreResolver storeResolver
    ) {
        this.varianteService = varianteService;
        this.storeResolver = storeResolver;
    }

    @PostMapping("/actualizarPrecio")
    public ResponseEntity<Void> actualizarPrecio(
            @RequestParam Long varianteId,
            @RequestParam BigDecimal precio,
            HttpServletRequest request
    ) {

        Store store =
                storeResolver.getCurrentStore(request);

        varianteService.actualizarPrecio(
                varianteId,
                precio,
                store
        );

        return ResponseEntity.ok().build();
    }
}
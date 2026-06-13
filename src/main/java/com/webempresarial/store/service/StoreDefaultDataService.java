package com.webempresarial.store.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.CategoriaRepository;

@Service
public class StoreDefaultDataService {

    private final CategoriaRepository categoriaRepository;

    public StoreDefaultDataService(
            CategoriaRepository categoriaRepository
    ) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public void createDefaults(Store store) {
        createDefaultCategories(store);
    }

    private void createDefaultCategories(Store store) {

        createCategoryIfMissing(store, "Destacados");
        createCategoryIfMissing(store, "Nuevos");
        createCategoryIfMissing(store, "Promociones");
    }

    private void createCategoryIfMissing(
            Store store,
            String nombre
    ) {

        boolean exists = categoriaRepository
                .existsByNombreIgnoreCaseAndStore(
                        nombre,
                        store
                );

        if (exists) {
            return;
        }

        Categoria categoria = new Categoria();
        categoria.setStore(store);
        categoria.setNombre(nombre);

        categoriaRepository.save(categoria);
    }
}
package com.webempresarial.store.service;

import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository
    ) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> obtenerTodas(Store store) {
        return categoriaRepository.findByStoreOrderByNombreAsc(store);
    }

    public Categoria obtenerPorId(Long id, Store store) {
        return categoriaRepository.findByIdAndStore(id, store)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Categoría no encontrada con ID: " + id
                        )
                );
    }

    public Categoria guardar(
            Categoria categoria,
            Store store
    ) {
        categoria.setStore(store);
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Long id, Store store) {
        Categoria categoria = obtenerPorId(id, store);
        categoriaRepository.delete(categoria);
    }

    public Categoria obtenerOCrearCategoria(
            String nombre,
            Store store
    ) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException(
                    "La categoría no puede ser nula o vacía"
            );
        }

        String nombreLimpio = nombre.trim();

        return categoriaRepository
                .findByNombreIgnoreCaseAndStore(
                        nombreLimpio,
                        store
                )
                .orElseGet(() -> {

                    Categoria nueva = new Categoria();
                    nueva.setNombre(nombreLimpio);
                    nueva.setStore(store);

                    return categoriaRepository.save(nueva);
                });
    }
}
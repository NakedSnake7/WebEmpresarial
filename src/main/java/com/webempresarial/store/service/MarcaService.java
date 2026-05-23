package com.webempresarial.store.service;

import com.webempresarial.store.model.Marca;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.MarcaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    public List<Marca> obtenerTodas(Store store) {
        return marcaRepository.findByStoreOrderByNombreAsc(store);
    }

    public Marca obtenerPorId(Long id, Store store) {

        if (id == null) return null;

        return marcaRepository.findByIdAndStore(id, store)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Marca no encontrada ID: " + id
                        )
                );
    }

    public Marca obtenerOCrear(
            String nombre,
            Store store
    ) {

        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        String nombreLimpio = nombre.trim();

        return marcaRepository
                .findByNombreIgnoreCaseAndStore(
                        nombreLimpio,
                        store
                )
                .orElseGet(() -> {

                    Marca nueva = new Marca();
                    nueva.setNombre(nombreLimpio);
                    nueva.setStore(store);

                    return marcaRepository.save(nueva);
                });
    }

    public void eliminar(Long id, Store store) {

        if (id == null) return;

        Marca marca = obtenerPorId(id, store);

        marcaRepository.delete(marca);
    }
}
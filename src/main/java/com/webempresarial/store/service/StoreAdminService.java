package com.webempresarial.store.service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreAdminService {

    private final StoreRepository storeRepository;

    public StoreAdminService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<Store> listarTiendas() {
        return storeRepository.findAll();
    }

    public Store guardar(Store store) {

        store.setDominio(
                store.getDominio()
                        .trim()
                        .toLowerCase()
                        .replace("https://", "")
                        .replace("http://", "")
                        .replace("/", "")
        );

        store.setTheme(store.getTheme().trim());

        return storeRepository.save(store);
    }

    public Store buscarPorId(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));
    }

    public void cambiarEstado(Long id) {
        Store store = buscarPorId(id);
        store.setActiva(!store.isActiva());
        storeRepository.save(store);
    }

    public void eliminar(Long id) {
        storeRepository.deleteById(id);
    }
}
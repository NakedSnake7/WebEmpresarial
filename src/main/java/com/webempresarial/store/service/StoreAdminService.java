package com.webempresarial.store.service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreAdminService {

    private final StoreRepository storeRepository;
    private final StoreProvisioningService storeProvisioningService;

    public StoreAdminService(
            StoreRepository storeRepository,
            StoreProvisioningService storeProvisioningService
    ) {
        this.storeRepository = storeRepository;
        this.storeProvisioningService = storeProvisioningService;
    }

    public List<Store> listarTiendas() {
        return storeRepository.findAllWithSubscription();
    }

    @Transactional
    @CacheEvict(value = "storesByDomain", allEntries = true)
    public Store guardar(Store store, String subscriptionType) {

        boolean isNewStore = store.getId() == null;

        normalizeStore(store);

        Store savedStore = storeRepository.save(store);

        if (isNewStore) {
            storeProvisioningService.provision(savedStore, subscriptionType);
        }

        return savedStore;
    }

    @CacheEvict(value = "storesByDomain", allEntries = true)
    public Store guardar(Store store) {
        return guardar(store, "INTERNAL");
    }

    private void normalizeStore(Store store) {

        store.setDominio(
                store.getDominio()
                        .trim()
                        .toLowerCase()
                        .replace("https://", "")
                        .replace("http://", "")
                        .replace("/", "")
        );

        if (store.getTheme() != null) {
            store.setTheme(store.getTheme().trim());
        }
    }



    public Store buscarPorId(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));
    }

    @CacheEvict(value = "storesByDomain", allEntries = true)
    public void cambiarEstado(Long id) {
        Store store = buscarPorId(id);
        store.setActiva(!store.isActiva());
        storeRepository.save(store);
    }

    @CacheEvict(value = "storesByDomain", allEntries = true)
    public void eliminar(Long id) {
        storeRepository.deleteById(id);
    }
}
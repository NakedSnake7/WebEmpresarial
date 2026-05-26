package com.webempresarial.store.service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class StoreContextService {

    private final StoreRepository storeRepository;

    public StoreContextService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Store getCurrentStore(HttpServletRequest request) {

        String rawHost = request.getHeader("X-Forwarded-Host");

        if (rawHost == null || rawHost.isBlank()) {
            rawHost = request.getServerName();
        }

        if (rawHost == null || rawHost.isBlank()) {
            throw new RuntimeException("No se pudo detectar el dominio actual");
        }

        final String host = normalizeHost(rawHost);

        return storeRepository.findByDominioAndActivaTrue(host)
                .orElseThrow(() -> new RuntimeException(
                        "Tienda no encontrada para dominio: " + host
                ));
    }

    private String normalizeHost(String host) {

        return host
                .trim()
                .toLowerCase()
                .replace("https://", "")
                .replace("http://", "")
                .split(":")[0]
                .replace("/", "");
    }
}
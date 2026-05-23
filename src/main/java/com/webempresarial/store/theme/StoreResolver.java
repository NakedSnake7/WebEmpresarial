package com.webempresarial.store.theme;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class StoreResolver {

    private final StoreRepository storeRepository;

    public StoreResolver(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Store getCurrentStore(HttpServletRequest request) {

        String rawHost = request.getHeader("X-Forwarded-Host");

        if (rawHost == null || rawHost.isBlank()) {
            rawHost = request.getServerName();
        }

        if (rawHost == null || rawHost.isBlank()) {
            throw new RuntimeException("No se pudo resolver el dominio actual");
        }

        final String host = rawHost.toLowerCase().split(":")[0];

        return storeRepository.findByDominioAndActivaTrue(host)
                .orElseThrow(() -> new RuntimeException(
                        "Tienda no encontrada para dominio: " + host
                ));
    }

    public String getCurrentTheme(HttpServletRequest request) {
        return getCurrentStore(request).getTheme();
    }
}
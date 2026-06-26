package com.webempresarial.store.dto.saas;

public record TopStoreUsageDTO(
        String storeName,
        String domain,
        Long total
) {}

package com.webempresarial.store.dto.module;

public record ModuleCardDTO(
        String name,
        String description,
        int featureCount,
        int sidebarSections
) {}
package com.webempresarial.store.dto.platform;

public record PlatformModuleDTO(
        String name,
        String description,
        int features,
        int dashboardWidgets,
        int sidebarSections,
        int permissions,
        int automations
) {}
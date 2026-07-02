package com.webempresarial.store.dto.marketplace;

public record MarketplaceModuleDTO(
        String name,
        String description,
        int featureCount,
        int sidebarSections,
        int dashboardWidgets,
        boolean installed,
        String badge
) {}
package com.webempresarial.store.feature.sidebar;

import com.webempresarial.store.model.Feature;

public record SidebarItemDefinition(
        String title,
        String icon,
        String url,
        Feature feature
) {}
package com.webempresarial.store.dto.sidebar;

import com.webempresarial.store.model.Feature;

public record SidebarItemDTO(
        String title,
        String icon,
        String url,
        Feature feature,
        boolean locked,
        String badge
) {}
package com.webempresarial.store.dto.dashboard;

import com.webempresarial.store.model.Feature;

public record DashboardWidgetDTO(
        String title,
        String subtitle,
        String icon,
        String url,
        Feature feature,
        boolean locked,
        String badge
) {}
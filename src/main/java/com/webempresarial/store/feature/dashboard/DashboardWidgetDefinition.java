package com.webempresarial.store.feature.dashboard;

import com.webempresarial.store.model.Feature;

public record DashboardWidgetDefinition(
        String title,
        String subtitle,
        String icon,
        String url,
        Feature feature,
        int order
) {}
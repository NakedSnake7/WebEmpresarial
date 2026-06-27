package com.webempresarial.store.dto.feature;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

public record FeatureCardDTO(
        Feature feature,
        String name,
        String description,
        String icon,
        String color,
        String url,

        String section,
        String sectionIcon,

        StorePlan minimumPlan,

        boolean available,
        boolean premium,
        boolean locked,
        boolean showInSidebar,
        boolean showInDashboard,
        boolean showInBilling,
        boolean showUpgradeCard,
        boolean trackUsage,

        String badge
) {}
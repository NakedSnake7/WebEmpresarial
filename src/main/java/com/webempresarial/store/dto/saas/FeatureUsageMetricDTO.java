package com.webempresarial.store.dto.saas;


public record FeatureUsageMetricDTO(
        String feature,
        String icon,
        String module,
        Long total
) {}
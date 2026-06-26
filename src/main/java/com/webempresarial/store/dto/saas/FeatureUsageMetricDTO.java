package com.webempresarial.store.dto.saas;

import com.webempresarial.store.model.Feature;

public record FeatureUsageMetricDTO(
        Feature feature,
        Long total
) {}
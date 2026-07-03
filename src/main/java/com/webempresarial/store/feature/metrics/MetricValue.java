package com.webempresarial.store.feature.metrics;

import java.time.LocalDateTime;

public record MetricValue(
        String code,
        String name,
        Number value,
        String unit,
        LocalDateTime measuredAt
) {}
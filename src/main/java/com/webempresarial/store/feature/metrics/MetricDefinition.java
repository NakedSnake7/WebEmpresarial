package com.webempresarial.store.feature.metrics;

public record MetricDefinition(
        String code,
        String name,
        String description,
        String unit
) {}
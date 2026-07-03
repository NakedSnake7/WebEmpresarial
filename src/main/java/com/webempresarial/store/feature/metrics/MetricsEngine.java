package com.webempresarial.store.feature.metrics;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricsEngine {

    private final List<MetricsProvider> providers;

    public MetricsEngine(List<MetricsProvider> providers) {
        this.providers = providers;
    }

    public List<MetricValue> collectAll() {
        return providers.stream()
                .flatMap(provider -> provider.collect().stream())
                .toList();
    }
}
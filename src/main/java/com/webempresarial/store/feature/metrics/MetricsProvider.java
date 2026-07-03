package com.webempresarial.store.feature.metrics;

import java.util.List;

public interface MetricsProvider {

    List<MetricValue> collect();
}
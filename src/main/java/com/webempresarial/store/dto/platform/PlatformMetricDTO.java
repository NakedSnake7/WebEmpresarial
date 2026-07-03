package com.webempresarial.store.dto.platform;

public record PlatformMetricDTO(
        String code,
        String name,
        Number value,
        String unit,
        String measuredAt
) {}
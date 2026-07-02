package com.webempresarial.store.feature.health;

import java.time.LocalDateTime;

public record HealthResult(
        String name,
        HealthStatus status,
        String message,
        LocalDateTime checkedAt
) {

    public static HealthResult up(String name, String message) {
        return new HealthResult(
                name,
                HealthStatus.UP,
                message,
                LocalDateTime.now()
        );
    }

    public static HealthResult degraded(String name, String message) {
        return new HealthResult(
                name,
                HealthStatus.DEGRADED,
                message,
                LocalDateTime.now()
        );
    }

    public static HealthResult down(String name, String message) {
        return new HealthResult(
                name,
                HealthStatus.DOWN,
                message,
                LocalDateTime.now()
        );
    }

    public static HealthResult unknown(String name, String message) {
        return new HealthResult(
                name,
                HealthStatus.UNKNOWN,
                message,
                LocalDateTime.now()
        );
    }
}
package com.webempresarial.store.feature.event;

import java.time.LocalDateTime;
import java.util.Map;

public record PlatformEvent(
        String name,
        String sourceModule,
        Object payload,
        Map<String, Object> metadata,
        LocalDateTime occurredAt
) {

    public static PlatformEvent of(
            String name,
            String sourceModule,
            Object payload
    ) {
        return new PlatformEvent(
                name,
                sourceModule,
                payload,
                Map.of(),
                LocalDateTime.now()
        );
    }

    public static PlatformEvent of(
            String name,
            String sourceModule,
            Object payload,
            Map<String, Object> metadata
    ) {
        return new PlatformEvent(
                name,
                sourceModule,
                payload,
                metadata != null ? metadata : Map.of(),
                LocalDateTime.now()
        );
    }
}
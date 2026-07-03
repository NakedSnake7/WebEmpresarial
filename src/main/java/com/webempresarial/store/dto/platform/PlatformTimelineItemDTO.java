package com.webempresarial.store.dto.platform;

public record PlatformTimelineItemDTO(
        String type,
        String title,
        String source,
        String status,
        String message,
        String occurredAt,
        long durationMs,
        String correlationId,
        String executionId,
        String parentExecutionId,
        String spanId
) {}
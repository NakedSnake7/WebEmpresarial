package com.webempresarial.store.dto.platform;

public record ExecutionNodeDetailDTO(
        String executionId,
        String parentExecutionId,
        String correlationId,
        String spanId,
        String type,
        String name,
        String source,
        boolean success,
        String message,
        String startedAt,
        String finishedAt,
        long durationMs
) {}
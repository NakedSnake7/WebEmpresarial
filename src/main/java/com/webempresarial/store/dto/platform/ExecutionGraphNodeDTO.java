package com.webempresarial.store.dto.platform;

public record ExecutionGraphNodeDTO(
        String id,
        String label,
        String type,
        String status,
        String source,
        long durationMs
) {}
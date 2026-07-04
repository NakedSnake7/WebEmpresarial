package com.webempresarial.store.dto.platform;

public record PositionedGraphNodeDTO(
        String id,
        String label,
        String type,
        String status,
        String source,
        long durationMs,
        int x,
        int y,
        int level
) {}
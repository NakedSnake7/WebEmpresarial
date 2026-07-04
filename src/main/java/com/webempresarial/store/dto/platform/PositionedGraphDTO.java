package com.webempresarial.store.dto.platform;

import java.util.List;

public record PositionedGraphDTO(
        String correlationId,
        long totalDurationMs,
        int totalNodes,
        int failedNodes,
        int successfulNodes,
        List<PositionedGraphNodeDTO> nodes,
        List<ExecutionGraphEdgeDTO> edges
) {}
package com.webempresarial.store.dto.platform;

import java.util.List;

public record ExecutionGraphDTO(
        String correlationId,
        long totalDurationMs,
        int totalNodes,
        int failedNodes,
        int successfulNodes,
        List<ExecutionGraphNodeDTO> nodes,
        List<ExecutionGraphEdgeDTO> edges
) {}
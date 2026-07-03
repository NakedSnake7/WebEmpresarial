package com.webempresarial.store.dto.platform;

import java.util.List;

public record ExecutionTraceDTO(
        String correlationId,
        List<ExecutionTraceNodeDTO> roots
) {}
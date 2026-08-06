package com.webempresarial.store.digitaltransformation.application.traceability;

import com.webempresarial.store.digitaltransformation.domain.traceability.*;

public record CreateTraceabilityLinkCommand(
        Long storeId,
        Long projectId,
        Long sourceNodeId,
        Long targetNodeId,
        TraceabilityRelationType relationType,
        TraceabilityStrength strength,
        TraceabilityOrigin origin,
        String rationale,
        String actor
) {
}
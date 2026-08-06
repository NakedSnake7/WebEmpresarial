package com.webempresarial.store.digitaltransformation.application.traceability;

import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityOrigin;

public record CreateTraceabilityNodeCommand(
        Long storeId,
        Long projectId,
        String nodeCode,
        TraceabilityNodeType nodeType,
        TraceabilityOrigin origin,
        String title,
        String description,
        String externalReference,
        String externalEntityType,
        boolean requiresReview,
        String actor
) {
}
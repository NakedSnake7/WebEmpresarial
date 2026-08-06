package com.webempresarial.store.digitaltransformation.application.traceability;

import com.webempresarial.store.digitaltransformation.domain.traceability.*;

public record TraceabilityNodeResult(
        Long id,
        Long projectId,
        String nodeCode,
        TraceabilityNodeType nodeType,
        TraceabilityNodeStatus status,
        TraceabilityOrigin origin,
        String title,
        String description,
        String externalReference,
        String externalEntityType,
        boolean requiresReview,
        String verifiedBy
) {

    public static TraceabilityNodeResult from(
            TraceabilityNode node
    ) {
        return new TraceabilityNodeResult(
                node.getId(),
                node.getProject().getId(),
                node.getNodeCode(),
                node.getNodeType(),
                node.getStatus(),
                node.getOrigin(),
                node.getTitle(),
                node.getDescription(),
                node.getExternalReference(),
                node.getExternalEntityType(),
                node.isRequiresReview(),
                node.getVerifiedBy()
        );
    }
}
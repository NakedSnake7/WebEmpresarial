package com.webempresarial.store.digitaltransformation.application.traceability;

import com.webempresarial.store.digitaltransformation.domain.traceability.*;

public record TraceabilityLinkResult(
        Long id,
        Long projectId,
        Long sourceNodeId,
        Long targetNodeId,
        TraceabilityRelationType relationType,
        TraceabilityStrength strength,
        TraceabilityLinkStatus status,
        TraceabilityOrigin origin,
        String rationale,
        boolean requiresReview,
        String verifiedBy
) {

    public static TraceabilityLinkResult from(
            TraceabilityLink link
    ) {
        return new TraceabilityLinkResult(
                link.getId(),
                link.getProject().getId(),
                link.getSourceNode().getId(),
                link.getTargetNode().getId(),
                link.getRelationType(),
                link.getStrength(),
                link.getStatus(),
                link.getOrigin(),
                link.getRationale(),
                link.isRequiresReview(),
                link.getVerifiedBy()
        );
    }
}
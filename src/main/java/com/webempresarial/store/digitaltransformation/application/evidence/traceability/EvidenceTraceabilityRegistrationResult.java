package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeStatus;

public record EvidenceTraceabilityRegistrationResult(
        Long evidenceId,
        String evidenceCode,
        boolean registered,
        boolean existing,
        Long traceabilityNodeId,
        String traceabilityNodeCode,
        TraceabilityNodeStatus nodeStatus,
        String decisionReason
) {

    public static EvidenceTraceabilityRegistrationResult skipped(
            Long evidenceId,
            String evidenceCode,
            String reason
    ) {
        return new EvidenceTraceabilityRegistrationResult(
                evidenceId,
                evidenceCode,
                false,
                false,
                null,
                null,
                null,
                reason
        );
    }
}
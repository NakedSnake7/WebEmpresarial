package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceStatus;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeStatus;

public record EvidenceTraceabilitySyncResult(
        Long evidenceId,
        String evidenceCode,
        EvidenceStatus evidenceStatus,
        Long nodeId,
        String nodeCode,
        TraceabilityNodeStatus nodeStatus,
        EvidenceTraceabilitySyncAction action,
        boolean changed,
        String reason
) {

    public EvidenceTraceabilitySyncResult {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "La razón de sincronización es obligatoria"
            );
        }
    }
}
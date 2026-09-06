package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

public interface EvidenceTraceabilitySynchronizer {

    EvidenceTraceabilitySyncResult synchronize(
            Long storeId,
            Long evidenceId,
            String actor
    );
}
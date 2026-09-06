package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;

public interface EvidenceTraceabilityDriftDetector {

    EvidenceTraceabilityDrift detect(
            SourceEvidence evidence,
            TraceabilityNode node
    );
}
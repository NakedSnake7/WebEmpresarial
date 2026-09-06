package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;

public interface EvidenceRegistrationPolicy {

    EvidenceRegistrationDecision evaluate(
            SourceEvidence evidence
    );
}
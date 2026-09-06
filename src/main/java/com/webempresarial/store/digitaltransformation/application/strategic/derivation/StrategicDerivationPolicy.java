package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.classification.StrategicClassificationResult;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;

public interface StrategicDerivationPolicy {

    StrategicDerivationDecision evaluate(
            SourceEvidence evidence,
            StrategicClassificationResult classification
    );
}
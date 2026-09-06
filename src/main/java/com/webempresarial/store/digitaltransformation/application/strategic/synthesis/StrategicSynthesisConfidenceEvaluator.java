package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

public interface StrategicSynthesisConfidenceEvaluator {

    StrategicSynthesisConfidence evaluate(
            StrategicChain chain,
            StrategicEvidenceCoverage evidenceCoverage
    );
}
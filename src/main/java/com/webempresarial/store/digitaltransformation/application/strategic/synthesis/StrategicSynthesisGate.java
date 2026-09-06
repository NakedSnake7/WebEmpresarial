package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicEvidenceCoverage;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisGateResult;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalResult;

public interface StrategicSynthesisGate {

    StrategicSynthesisGateResult evaluate(
            StrategicTraversalResult traversal,
            StrategicEvidenceCoverage evidenceCoverage
    );
}
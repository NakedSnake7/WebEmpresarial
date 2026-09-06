package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChain;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicEvidenceCoverage;

public interface StrategicChainEvidenceCoverageEvaluator {

    StrategicEvidenceCoverage evaluate(
            StrategicChain chain
    );
}
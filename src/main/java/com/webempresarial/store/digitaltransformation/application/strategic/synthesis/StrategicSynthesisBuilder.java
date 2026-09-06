package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

public interface StrategicSynthesisBuilder {

    StrategicSynthesis build(
            StrategicChain chain,
            StrategicEvidenceCoverage evidenceCoverage,
            StrategicSynthesisGateResult gateResult
    );
}
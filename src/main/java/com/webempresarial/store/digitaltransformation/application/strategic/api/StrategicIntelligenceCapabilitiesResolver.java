package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

public interface StrategicIntelligenceCapabilitiesResolver {

    StrategicIntelligenceCapabilitiesResponse resolve(
            StrategicTraversalResult traversal,
            StrategicEvidenceCoverage coverage,
            StrategicSynthesisGateResult gateResult,
            StoredStrategicSynthesis deterministic,
            StoredStrategicSynthesis ai
    );
}
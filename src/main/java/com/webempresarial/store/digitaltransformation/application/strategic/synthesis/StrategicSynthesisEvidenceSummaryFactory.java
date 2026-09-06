package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

public interface StrategicSynthesisEvidenceSummaryFactory {

    StrategicSynthesisEvidenceSummary create(
            StrategicEvidenceCoverage coverage
    );
}
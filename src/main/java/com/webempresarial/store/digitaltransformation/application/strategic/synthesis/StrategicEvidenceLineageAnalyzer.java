package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicArtifactEvidenceSupport;

public interface StrategicEvidenceLineageAnalyzer {

    StrategicArtifactEvidenceSupport analyze(
            StrategicArtifact artifact
    );
}
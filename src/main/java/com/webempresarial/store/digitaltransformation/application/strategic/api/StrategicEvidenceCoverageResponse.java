package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicEvidenceCoverageStatus;

public record StrategicEvidenceCoverageResponse(

        StrategicEvidenceCoverageStatus status,

        int coveragePercentage,

        int supportedArtifacts,

        int directArtifacts,

        int weakArtifacts,

        int unsupportedArtifacts,

        boolean fullySupported,

        boolean canProceedToSynthesis

) {
}
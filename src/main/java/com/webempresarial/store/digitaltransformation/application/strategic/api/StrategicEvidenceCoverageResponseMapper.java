package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicEvidenceCoverage;

public final class StrategicEvidenceCoverageResponseMapper {

    private StrategicEvidenceCoverageResponseMapper() {
    }

    public static StrategicEvidenceCoverageResponse toResponse(
            StrategicEvidenceCoverage coverage
    ) {
        if (coverage == null) {
            return null;
        }

        return new StrategicEvidenceCoverageResponse(
                coverage.getStatus(),
                coverage.coveragePercentage(),
                coverage.getSupportedArtifacts(),
                coverage.getDirectArtifacts(),
                coverage.getWeakArtifacts(),
                coverage.getUnsupportedArtifacts(),
                coverage.isFullySupported(),
                coverage.canProceedToSynthesis()
        );
    }
}
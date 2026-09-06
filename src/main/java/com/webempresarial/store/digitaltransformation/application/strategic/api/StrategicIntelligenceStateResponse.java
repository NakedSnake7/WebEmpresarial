package com.webempresarial.store.digitaltransformation.application.strategic.api;

import java.util.List;

public record StrategicIntelligenceStateResponse(

        Long projectId,

        Long findingArtifactId,
        
        StrategicChainSummaryResponse chain,

        StrategicEvidenceCoverageResponse evidenceCoverage,

        StrategicSynthesisResponse deterministicSynthesis,

        StrategicSynthesisResponse aiSynthesis,

        List<StrategicReviewSummaryResponse> reviews,

        StrategicIntelligenceCapabilitiesResponse capabilities,
        
        Long reviewableSynthesisId

) {
}
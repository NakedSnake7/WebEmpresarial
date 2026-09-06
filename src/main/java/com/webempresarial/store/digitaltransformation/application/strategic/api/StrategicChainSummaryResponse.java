package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalStatus;

import java.util.List;

public record StrategicChainSummaryResponse(

        StrategicTraversalStatus traversalStatus,

        StrategicArtifactSummaryResponse finding,

        StrategicArtifactSummaryResponse businessProblem,

        StrategicArtifactSummaryResponse businessObjective,

        StrategicArtifactSummaryResponse strategicOpportunity,

        boolean complete,

        boolean ambiguous,

        boolean canBuildChain,

        List<StrategicChainGapResponse> gaps,

        List<StrategicTraversalAmbiguityResponse> ambiguities

) {
}
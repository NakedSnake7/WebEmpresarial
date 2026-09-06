package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalAmbiguity;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalResult;

import java.util.Objects;

public final class StrategicChainSummaryResponseMapper {

    private StrategicChainSummaryResponseMapper() {
    }

    public static StrategicChainSummaryResponse toResponse(
            StrategicTraversalResult traversal
    ) {
        Objects.requireNonNull(
                traversal,
                "El resultado del traversal es obligatorio"
        );

        return new StrategicChainSummaryResponse(
                traversal.getStatus(),
                artifact(
                        traversal.getFinding()
                ),
                artifact(
                        traversal.getBusinessProblem()
                ),
                artifact(
                        traversal.getBusinessObjective()
                ),
                artifact(
                        traversal.getStrategicOpportunity()
                ),
                traversal.isComplete(),
                traversal.isAmbiguous(),
                traversal.canBuildChain(),
                traversal.getGaps()
                        .stream()
                        .map(gap ->
                                new StrategicChainGapResponse(
                                        gap.type(),
                                        gap.description()
                                )
                        )
                        .toList(),
                traversal.getAmbiguities()
                        .stream()
                        .map(
                                StrategicChainSummaryResponseMapper::ambiguity
                        )
                        .toList()
        );
    }

    private static StrategicArtifactSummaryResponse artifact(
            StrategicArtifact artifact
    ) {
        if (artifact == null) {
            return null;
        }

        return new StrategicArtifactSummaryResponse(
                artifact.getId(),
                artifact.getArtifactCode(),
                artifact.getArtifactType(),
                artifact.getStatement(),
                artifact.getStatus(),
                artifact.getConfidence(),
                artifact.getPriority(),
                artifact.isRequiresReview(),
                artifact.canDriveImplementation()
        );
    }

    private static StrategicTraversalAmbiguityResponse ambiguity(
            StrategicTraversalAmbiguity ambiguity
    ) {
        return new StrategicTraversalAmbiguityResponse(
                ambiguity.type(),
                ambiguity.sourceArtifactCode(),
                ambiguity.candidateArtifactCodes(),
                ambiguity.description()
        );
    }
}
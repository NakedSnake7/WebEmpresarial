package com.webempresarial.store.digitaltransformation.application.strategic;

import com.webempresarial.store.digitaltransformation.domain.strategic.*;

public record StrategicArtifactResult(
        Long id,
        Long projectId,
        String artifactCode,
        StrategicArtifactType artifactType,
        StrategicArtifactStatus status,
        StrategicConfidence confidence,
        StrategicArtifactOrigin origin,
        StrategicPriority priority,
        String statement,
        String rationale,
        String businessImplication,
        boolean requiresReview,
        boolean canDriveImplementation,
        String verifiedBy
) {

    public static StrategicArtifactResult from(
            StrategicArtifact artifact
    ) {
        return new StrategicArtifactResult(
                artifact.getId(),
                artifact.getProject().getId(),
                artifact.getArtifactCode(),
                artifact.getArtifactType(),
                artifact.getStatus(),
                artifact.getConfidence(),
                artifact.getOrigin(),
                artifact.getPriority(),
                artifact.getStatement(),
                artifact.getRationale(),
                artifact.getBusinessImplication(),
                artifact.isRequiresReview(),
                artifact.canDriveImplementation(),
                artifact.getVerifiedBy()
        );
    }
}
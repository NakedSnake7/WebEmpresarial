package com.webempresarial.store.digitaltransformation.application.strategic;

import com.webempresarial.store.digitaltransformation.domain.strategic.*;

public record CreateStrategicArtifactCommand(
        Long storeId,
        Long projectId,
        StrategicArtifactType artifactType,
        StrategicConfidence confidence,
        StrategicArtifactOrigin origin,
        String statement,
        String rationale,
        String businessImplication,
        String actor
) {
}
package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicPriority;

public record StrategicArtifactSummaryResponse(

        Long id,

        String code,

        StrategicArtifactType type,

        String statement,

        StrategicArtifactStatus status,

        StrategicConfidence confidence,

        StrategicPriority priority,

        boolean requiresReview,

        boolean canDriveImplementation

) {
}
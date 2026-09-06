package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;

public record StrategicFindingOptionResponse(

        Long id,

        String code,

        String statement,

        StrategicArtifactStatus status,

        StrategicConfidence confidence,

        boolean requiresReview

) {
}
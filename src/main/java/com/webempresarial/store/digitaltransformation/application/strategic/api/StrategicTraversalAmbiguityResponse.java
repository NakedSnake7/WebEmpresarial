package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalAmbiguityType;

import java.util.List;

public record StrategicTraversalAmbiguityResponse(

        StrategicTraversalAmbiguityType type,

        String sourceArtifactCode,

        List<String> candidateArtifactCodes,

        String description

) {
}
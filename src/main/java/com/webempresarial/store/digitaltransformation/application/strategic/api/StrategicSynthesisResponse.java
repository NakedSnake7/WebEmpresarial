package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import java.time.Instant;
import java.util.List;

public record StrategicSynthesisResponse(

        Long id,

        String thesis,

        StrategicSynthesisConfidence confidence,

        StrategicSynthesisOrigin origin,

        StrategicSynthesisStatus status,

        List<String> sourceArtifactCodes,

        Instant createdAt,

        boolean requiresReview,

        boolean approved,

        boolean rejected

) {
}
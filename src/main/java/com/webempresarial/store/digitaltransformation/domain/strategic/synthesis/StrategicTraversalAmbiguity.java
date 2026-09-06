package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;
import java.util.Objects;

public record StrategicTraversalAmbiguity(
        StrategicTraversalAmbiguityType type,
        String sourceArtifactCode,
        List<String> candidateArtifactCodes,
        String description
) {

    public StrategicTraversalAmbiguity {
        Objects.requireNonNull(
                type,
                "El tipo de ambigüedad es obligatorio"
        );

        if (sourceArtifactCode == null
                || sourceArtifactCode.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del artefacto origen es obligatorio"
            );
        }

        candidateArtifactCodes =
                candidateArtifactCodes == null
                        ? List.of()
                        : List.copyOf(candidateArtifactCodes);

        if (candidateArtifactCodes.size() < 2) {
            throw new IllegalArgumentException(
                    "Una ambigüedad debe contener al menos dos candidatos"
            );
        }

        if (description == null
                || description.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción de la ambigüedad es obligatoria"
            );
        }

        sourceArtifactCode =
                sourceArtifactCode.trim();

        description =
                description.trim();
    }
}
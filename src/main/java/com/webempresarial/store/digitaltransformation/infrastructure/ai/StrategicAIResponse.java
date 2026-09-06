package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import java.util.List;
import java.util.Objects;

public record StrategicAIResponse(
        String interpretedThesis,
        String executiveNarrative,
        List<String> referencedArtifactCodes
) {

    public StrategicAIResponse {
        interpretedThesis =
                requireText(
                        interpretedThesis,
                        "La tesis interpretada es obligatoria"
                );

        if (executiveNarrative != null) {
            executiveNarrative =
                    executiveNarrative.trim();

            if (executiveNarrative.isBlank()) {
                executiveNarrative = null;
            }
        }

        referencedArtifactCodes =
                referencedArtifactCodes == null
                        ? List.of()
                        : referencedArtifactCodes.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(value -> !value.isBlank())
                                .distinct()
                                .toList();
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}
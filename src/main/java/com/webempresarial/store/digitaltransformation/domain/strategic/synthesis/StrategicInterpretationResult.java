package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;

public final class StrategicInterpretationResult {

    private final String interpretedThesis;

    private final String executiveNarrative;

    private final List<String> referencedArtifactCodes;

    private StrategicInterpretationResult(
            String interpretedThesis,
            String executiveNarrative,
            List<String> referencedArtifactCodes
    ) {
        this.interpretedThesis =
                requireText(
                        interpretedThesis,
                        "La tesis interpretada es obligatoria"
                );

        this.executiveNarrative =
                normalizeOptional(
                        executiveNarrative
                );

        this.referencedArtifactCodes =
                referencedArtifactCodes == null
                        ? List.of()
                        : referencedArtifactCodes.stream()
                                .filter(code ->
                                        code != null
                                                && !code.isBlank()
                                )
                                .map(String::trim)
                                .distinct()
                                .toList();
    }

    public static StrategicInterpretationResult of(
            String interpretedThesis,
            String executiveNarrative,
            List<String> referencedArtifactCodes
    ) {
        return new StrategicInterpretationResult(
                interpretedThesis,
                executiveNarrative,
                referencedArtifactCodes
        );
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

    private static String normalizeOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public String getInterpretedThesis() {
        return interpretedThesis;
    }

    public String getExecutiveNarrative() {
        return executiveNarrative;
    }

    public List<String> getReferencedArtifactCodes() {
        return referencedArtifactCodes;
    }
}
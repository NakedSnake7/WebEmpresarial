package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import java.util.List;
import java.util.Objects;

public record StrategicAIRequest(
        String systemInstruction,
        String taskInstruction,
        String finding,
        String businessProblem,
        String businessObjective,
        String strategicOpportunity,
        String deterministicThesis,
        List<String> sourceArtifactCodes,
        List<String> constraints
) {

    public StrategicAIRequest {
        systemInstruction =
                requireText(
                        systemInstruction,
                        "La instrucción de sistema es obligatoria"
                );

        taskInstruction =
                requireText(
                        taskInstruction,
                        "La instrucción de tarea es obligatoria"
                );

        finding =
                requireText(
                        finding,
                        "El finding es obligatorio"
                );

        businessProblem =
                requireText(
                        businessProblem,
                        "El problema de negocio es obligatorio"
                );

        businessObjective =
                requireText(
                        businessObjective,
                        "El objetivo de negocio es obligatorio"
                );

        strategicOpportunity =
                requireText(
                        strategicOpportunity,
                        "La oportunidad estratégica es obligatoria"
                );

        deterministicThesis =
                requireText(
                        deterministicThesis,
                        "La tesis determinista es obligatoria"
                );

        if (sourceArtifactCodes == null
                || sourceArtifactCodes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Los artefactos fuente son obligatorios"
            );
        }

        sourceArtifactCodes =
                sourceArtifactCodes.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .distinct()
                        .toList();

        if (constraints == null
                || constraints.isEmpty()) {
            throw new IllegalArgumentException(
                    "Los constraints son obligatorios"
            );
        }

        constraints =
                constraints.stream()
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
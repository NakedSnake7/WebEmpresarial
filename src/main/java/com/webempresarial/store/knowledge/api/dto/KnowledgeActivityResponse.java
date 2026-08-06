package com.webempresarial.store.knowledge.api.dto;

import java.time.LocalDateTime;

public record KnowledgeActivityResponse(

        Long knowledgeObjectId,

        Long versionId,

        String code,

        String title,

        String eventType,

        String actor,

        LocalDateTime occurredAt
) {

    public KnowledgeActivityResponse {
        if (knowledgeObjectId == null
                || knowledgeObjectId <= 0) {

            throw new IllegalArgumentException(
                    "El knowledgeObjectId debe ser válido"
            );
        }

        if (versionId != null
                && versionId <= 0) {

            throw new IllegalArgumentException(
                    "El versionId debe ser válido"
            );
        }

        code = normalizeRequired(
                code,
                "El código del conocimiento es obligatorio"
        );

        title = normalizeOptional(
                title
        );

        eventType = normalizeRequired(
                eventType,
                "El tipo de actividad es obligatorio"
        );

        actor = normalizeOptional(
                actor
        );

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de actividad es obligatoria"
            );
        }
    }

    private static String normalizeRequired(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    message
            );
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
}
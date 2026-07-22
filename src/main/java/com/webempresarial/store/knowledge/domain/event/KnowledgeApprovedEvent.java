package com.webempresarial.store.knowledge.domain.event;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento publicado cuando un KnowledgeObject pasa
 * correctamente de IN_REVIEW a APPROVED.
 */
public record KnowledgeApprovedEvent(

        Long knowledgeObjectId,

        Long storeId,

        String code,

        KnowledgeStatus previousStatus,

        KnowledgeStatus currentStatus,

        long versionCount,

        String latestSemanticVersion,

        String actor,

        LocalDateTime occurredAt
) {

    public KnowledgeApprovedEvent {
        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        code = normalizeRequired(
                code,
                "El código es obligatorio"
        );

        previousStatus = Objects.requireNonNull(
                previousStatus,
                "El estado anterior es obligatorio"
        );

        currentStatus = Objects.requireNonNull(
                currentStatus,
                "El estado actual es obligatorio"
        );

        if (versionCount <= 0) {
            throw new IllegalArgumentException(
                    "El número de versiones debe ser mayor que cero"
            );
        }

        latestSemanticVersion = normalizeRequired(
                latestSemanticVersion,
                "La versión semántica más reciente es obligatoria"
        );

        actor = normalizeRequired(
                actor,
                "El actor es obligatorio"
        );

        occurredAt = Objects.requireNonNull(
                occurredAt,
                "La fecha del evento es obligatoria"
        );
    }

    private static void validatePositiveId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String normalizeRequired(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}
package com.webempresarial.store.knowledge.domain.event;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento publicado cuando un KnowledgeObject deja de estar
 * disponible como conocimiento publicado.
 */
public record KnowledgeArchivedEvent(

        Long knowledgeObjectId,

        Long storeId,

        String code,

        Long currentVersionId,

        String currentSemanticVersion,

        KnowledgeStatus previousStatus,

        KnowledgeStatus currentStatus,

        String reason,

        String actor,

        LocalDateTime occurredAt
) {

    public KnowledgeArchivedEvent {
        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        validatePositiveId(
                currentVersionId,
                "El identificador de la versión vigente debe ser válido"
        );

        code = normalizeRequired(
                code,
                "El código es obligatorio"
        );

        currentSemanticVersion = normalizeRequired(
                currentSemanticVersion,
                "La versión semántica vigente es obligatoria"
        );

        previousStatus = Objects.requireNonNull(
                previousStatus,
                "El estado anterior es obligatorio"
        );

        currentStatus = Objects.requireNonNull(
                currentStatus,
                "El estado actual es obligatorio"
        );

        reason = normalizeRequired(
                reason,
                "La razón de archivado es obligatoria"
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
package com.webempresarial.store.knowledge.domain.event;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento publicado cuando una versión se convierte
 * en la versión vigente de un KnowledgeObject.
 */
public record KnowledgePublishedEvent(

        Long knowledgeObjectId,

        Long storeId,

        String code,

        Long knowledgeVersionId,

        String semanticVersion,

        KnowledgeStatus previousStatus,

        KnowledgeStatus currentStatus,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        String actor,

        LocalDateTime occurredAt
) {

    public KnowledgePublishedEvent {
        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        validatePositiveId(
                knowledgeVersionId,
                "El identificador de la versión debe ser válido"
        );

        code = normalizeRequired(
                code,
                "El código es obligatorio"
        );

        semanticVersion = normalizeRequired(
                semanticVersion,
                "La versión semántica es obligatoria"
        );

        previousStatus = Objects.requireNonNull(
                previousStatus,
                "El estado anterior es obligatorio"
        );

        currentStatus = Objects.requireNonNull(
                currentStatus,
                "El estado actual es obligatorio"
        );

        validFrom = Objects.requireNonNull(
                validFrom,
                "La fecha de inicio de vigencia es obligatoria"
        );

        if (validUntil != null
                && !validUntil.isAfter(validFrom)) {

            throw new IllegalArgumentException(
                    "La fecha final de vigencia debe ser posterior "
                            + "a la fecha inicial"
            );
        }

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
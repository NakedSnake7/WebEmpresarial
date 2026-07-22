package com.webempresarial.store.knowledge.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando una KnowledgeObjectVersion
 * ha sido creada y persistida correctamente.
 */
public record KnowledgeVersionCreatedEvent(

        Long knowledgeVersionId,

        Long knowledgeObjectId,

        Long storeId,

        String semanticVersion,

        String title,

        String contentFormat,

        BigDecimal confidence,

        String actor,

        LocalDateTime occurredAt
) {

    public KnowledgeVersionCreatedEvent {
        validatePositiveId(
                knowledgeVersionId,
                "El identificador de la versión debe ser válido"
        );

        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        semanticVersion = normalizeRequired(
                semanticVersion,
                "La versión semántica es obligatoria"
        );

        title = normalizeRequired(
                title,
                "El título es obligatorio"
        );

        contentFormat = normalizeRequired(
                contentFormat,
                "El formato de contenido es obligatorio"
        );

        if (confidence == null) {
            throw new IllegalArgumentException(
                    "El nivel de confianza es obligatorio"
            );
        }

        actor = normalizeRequired(
                actor,
                "El actor es obligatorio"
        );

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "La fecha del evento es obligatoria"
            );
        }
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
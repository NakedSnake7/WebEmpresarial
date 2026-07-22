package com.webempresarial.store.knowledge.application.command;

import java.time.LocalDateTime;

/**
 * Comando para publicar una versión aprobada de conocimiento.
 */
public record PublishKnowledgeCommand(

        Long storeId,

        Long knowledgeObjectId,

        Long knowledgeVersionId,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        String actor
) {

    public static final int MAX_ACTOR_LENGTH = 150;

    public PublishKnowledgeCommand {
        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        validatePositiveId(
                knowledgeVersionId,
                "El identificador de KnowledgeObjectVersion debe ser válido"
        );

        if (validFrom == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio de vigencia es obligatoria"
            );
        }

        if (validUntil != null
                && !validUntil.isAfter(validFrom)) {

            throw new IllegalArgumentException(
                    "La fecha final de vigencia debe ser posterior "
                            + "a la fecha inicial"
            );
        }

        actor = normalizeActor(actor);
    }

    private static void validatePositiveId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String normalizeActor(String actor) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "El actor es obligatorio"
            );
        }

        String normalized = actor.trim();

        if (normalized.length() > MAX_ACTOR_LENGTH) {
            throw new IllegalArgumentException(
                    "El actor no puede superar "
                            + MAX_ACTOR_LENGTH
                            + " caracteres"
            );
        }

        return normalized;
    }
}
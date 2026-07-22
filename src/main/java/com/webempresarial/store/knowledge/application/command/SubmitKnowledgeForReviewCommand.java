package com.webempresarial.store.knowledge.application.command;

/**
 * Comando para enviar un KnowledgeObject a revisión.
 */
public record SubmitKnowledgeForReviewCommand(

        Long storeId,

        Long knowledgeObjectId,

        String actor
) {

    public static final int MAX_ACTOR_LENGTH = 150;

    public SubmitKnowledgeForReviewCommand {
        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

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
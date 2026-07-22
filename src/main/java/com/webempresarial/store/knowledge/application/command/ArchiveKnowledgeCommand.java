package com.webempresarial.store.knowledge.application.command;

/**
 * Comando para archivar temporalmente un KnowledgeObject publicado.
 */
public record ArchiveKnowledgeCommand(

        Long storeId,

        Long knowledgeObjectId,

        String reason,

        String actor
) {

    public static final int MAX_REASON_LENGTH = 500;
    public static final int MAX_ACTOR_LENGTH = 150;

    public ArchiveKnowledgeCommand {
        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        reason = normalizeRequired(
                reason,
                "La razón de archivado es obligatoria",
                MAX_REASON_LENGTH
        );

        actor = normalizeRequired(
                actor,
                "El actor es obligatorio",
                MAX_ACTOR_LENGTH
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
            String message,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "El valor no puede superar "
                            + maxLength
                            + " caracteres"
            );
        }

        return normalized;
    }
}
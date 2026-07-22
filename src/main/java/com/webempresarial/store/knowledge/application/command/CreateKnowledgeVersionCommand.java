package com.webempresarial.store.knowledge.application.command;

import java.math.BigDecimal;

/**
 * Comando de aplicación para crear una nueva versión
 * dentro de un KnowledgeObject existente.
 *
 * <p>El comando utiliza valores simples. La capa de aplicación
 * será responsable de construir SemanticVersion y
 * KnowledgeConfidence.</p>
 */
public record CreateKnowledgeVersionCommand(

        Long storeId,

        Long knowledgeObjectId,

        String semanticVersion,

        String title,

        String summary,

        String content,

        String contentFormat,

        BigDecimal confidence,

        String sourceReference,

        String actor
) {

    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_SUMMARY_LENGTH = 1000;
    public static final int MAX_FORMAT_LENGTH = 50;
    public static final int MAX_SOURCE_REFERENCE_LENGTH = 500;
    public static final int MAX_ACTOR_LENGTH = 150;

    public CreateKnowledgeVersionCommand {
        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        semanticVersion = normalizeRequired(
                semanticVersion,
                "La versión semántica es obligatoria",
                50
        );

        title = normalizeRequired(
                title,
                "El título es obligatorio",
                MAX_TITLE_LENGTH
        );

        summary = normalizeRequired(
                summary,
                "El resumen es obligatorio",
                MAX_SUMMARY_LENGTH
        );

        content = normalizeContent(content);

        contentFormat = normalizeRequired(
                contentFormat,
                "El formato del contenido es obligatorio",
                MAX_FORMAT_LENGTH
        );

        if (confidence == null) {
            throw new IllegalArgumentException(
                    "El nivel de confianza es obligatorio"
            );
        }

        sourceReference = normalizeOptional(
                sourceReference,
                MAX_SOURCE_REFERENCE_LENGTH
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

    private static String normalizeContent(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido es obligatorio"
            );
        }

        return value.trim();
    }

    private static String normalizeOptional(
            String value,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
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
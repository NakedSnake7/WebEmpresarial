package com.webempresarial.store.knowledge.application.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado producido después de crear una nueva
 * KnowledgeObjectVersion.
 *
 * <p>No expone directamente la entidad JPA.</p>
 */
public record CreateKnowledgeVersionResult(

        Long id,

        Long knowledgeObjectId,

        Long storeId,

        String semanticVersion,

        String title,

        String summary,

        String contentFormat,

        BigDecimal confidence,

        String sourceReference,

        String createdBy,

        LocalDateTime createdAt,

        Long lockVersion
) {

    public CreateKnowledgeVersionResult {
        validatePositiveId(
                id,
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

        summary = normalizeRequired(
                summary,
                "El resumen es obligatorio"
        );

        contentFormat = normalizeRequired(
                contentFormat,
                "El formato del contenido es obligatorio"
        );

        if (confidence == null) {
            throw new IllegalArgumentException(
                    "El nivel de confianza es obligatorio"
            );
        }

        sourceReference = normalizeOptional(sourceReference);

        createdBy = normalizeRequired(
                createdBy,
                "El actor creador es obligatorio"
        );

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria"
            );
        }

        if (lockVersion == null || lockVersion < 0) {
            throw new IllegalArgumentException(
                    "La versión de bloqueo debe ser válida"
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

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
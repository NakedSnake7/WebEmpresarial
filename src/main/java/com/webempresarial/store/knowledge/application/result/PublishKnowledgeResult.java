package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Resultado de la publicación de un KnowledgeObject.
 */
public record PublishKnowledgeResult(

        Long knowledgeObjectId,

        Long storeId,

        String code,

        Long currentVersionId,

        String currentSemanticVersion,

        KnowledgeStatus previousStatus,

        KnowledgeStatus currentStatus,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        String updatedBy,

        LocalDateTime updatedAt,

        Long lockVersion
) {

    public PublishKnowledgeResult {
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

        updatedBy = normalizeRequired(
                updatedBy,
                "El actor actualizador es obligatorio"
        );

        updatedAt = Objects.requireNonNull(
                updatedAt,
                "La fecha de actualización es obligatoria"
        );

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
}
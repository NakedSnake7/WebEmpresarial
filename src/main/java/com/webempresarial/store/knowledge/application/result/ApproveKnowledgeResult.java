package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Resultado del caso de uso de aprobación de conocimiento.
 */
public record ApproveKnowledgeResult(

        Long knowledgeObjectId,

        Long storeId,

        String code,

        KnowledgeStatus previousStatus,

        KnowledgeStatus currentStatus,

        long versionCount,

        String latestSemanticVersion,

        String updatedBy,

        LocalDateTime updatedAt,

        Long lockVersion
) {

    public ApproveKnowledgeResult {
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
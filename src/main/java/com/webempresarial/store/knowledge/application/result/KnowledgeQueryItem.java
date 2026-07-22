package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representación resumida de un KnowledgeObject dentro
 * de los resultados del Query Engine.
 *
 * <p>No incluye el contenido LONGTEXT completo. Para consumir
 * contenido publicado debe utilizarse KnowledgeResolver.</p>
 */
public record KnowledgeQueryItem(

        Long knowledgeObjectId,

        Long storeId,

        String code,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeStatus status,

        KnowledgeContextType contextType,

        String contextReference,

        Long currentVersionId,

        String currentSemanticVersion,

        String title,

        String summary,

        String contentFormat,

        BigDecimal confidence,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {

    public KnowledgeQueryItem {
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

        typeCode = Objects.requireNonNull(
                typeCode,
                "KnowledgeTypeCode es obligatorio"
        );

        domain = Objects.requireNonNull(
                domain,
                "KnowledgeDomain es obligatorio"
        );

        classification = Objects.requireNonNull(
                classification,
                "KnowledgeClassification es obligatoria"
        );

        riskLevel = Objects.requireNonNull(
                riskLevel,
                "KnowledgeRiskLevel es obligatorio"
        );

        status = Objects.requireNonNull(
                status,
                "KnowledgeStatus es obligatorio"
        );

        contextType = Objects.requireNonNull(
                contextType,
                "KnowledgeContextType es obligatorio"
        );

        contextReference = normalizeRequired(
                contextReference,
                "La referencia del contexto es obligatoria"
        );

        if (currentVersionId != null && currentVersionId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la versión vigente debe ser válido"
            );
        }

        currentSemanticVersion =
                normalizeOptional(currentSemanticVersion);

        title = normalizeOptional(title);
        summary = normalizeOptional(summary);
        contentFormat = normalizeOptional(contentFormat);

        validateCurrentVersionFields(
                currentVersionId,
                currentSemanticVersion,
                title,
                summary,
                contentFormat,
                confidence
        );

        if (confidence != null) {
            validateConfidence(confidence);
        }

        if (validUntil != null
                && validFrom == null) {

            throw new IllegalArgumentException(
                    "No puede existir validUntil sin validFrom"
            );
        }

        if (validFrom != null
                && validUntil != null
                && !validUntil.isAfter(validFrom)) {

            throw new IllegalArgumentException(
                    "La fecha final de vigencia debe ser posterior "
                            + "a la fecha inicial"
            );
        }

        createdAt = Objects.requireNonNull(
                createdAt,
                "La fecha de creación es obligatoria"
        );

        updatedAt = Objects.requireNonNull(
                updatedAt,
                "La fecha de actualización es obligatoria"
        );
    }

    /**
     * Indica si el objeto tiene actualmente una versión seleccionada.
     */
    public boolean hasCurrentVersion() {
        return currentVersionId != null;
    }

    /**
     * Indica si el conocimiento está publicado y vigente
     * en el momento proporcionado.
     */
    public boolean isEffectiveAt(LocalDateTime moment) {
        Objects.requireNonNull(
                moment,
                "El momento de consulta es obligatorio"
        );

        if (status != KnowledgeStatus.PUBLISHED
                || !hasCurrentVersion()
                || validFrom == null) {

            return false;
        }

        boolean alreadyStarted =
                !moment.isBefore(validFrom);

        boolean notExpired =
                validUntil == null
                        || moment.isBefore(validUntil);

        return alreadyStarted && notExpired;
    }

    /**
     * Determina si la versión vigente alcanza el nivel
     * de confianza indicado.
     */
    public boolean hasMinimumConfidence(
            BigDecimal minimum
    ) {
        Objects.requireNonNull(
                minimum,
                "La confianza mínima es obligatoria"
        );

        validateConfidence(minimum);

        return confidence != null
                && confidence.compareTo(minimum) >= 0;
    }

    private static void validateCurrentVersionFields(
            Long currentVersionId,
            String currentSemanticVersion,
            String title,
            String summary,
            String contentFormat,
            BigDecimal confidence
    ) {
        boolean hasVersionId =
                currentVersionId != null;

        boolean hasAnyVersionMetadata =
                currentSemanticVersion != null
                        || title != null
                        || summary != null
                        || contentFormat != null
                        || confidence != null;

        if (!hasVersionId && hasAnyVersionMetadata) {
            throw new IllegalArgumentException(
                    "No puede existir metadata de versión "
                            + "sin currentVersionId"
            );
        }

        if (hasVersionId
                && (
                currentSemanticVersion == null
                        || title == null
                        || summary == null
                        || contentFormat == null
                        || confidence == null
        )) {
            throw new IllegalArgumentException(
                    "La metadata de la versión vigente está incompleta"
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

    private static void validateConfidence(
            BigDecimal value
    ) {
        if (value.compareTo(BigDecimal.ZERO) < 0
                || value.compareTo(BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "El nivel de confianza debe estar entre 0 y 1"
            );
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

    private static String normalizeOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
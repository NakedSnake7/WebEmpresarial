package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representación inmutable del conocimiento resuelto.
 *
 * <p>Este objeto es el contrato que podrán consumir otros motores
 * de WebEmpresarial sin conocer entidades JPA ni detalles de
 * persistencia.</p>
 */
public record KnowledgeSnapshot(

        Long knowledgeObjectId,

        Long knowledgeVersionId,

        Long storeId,

        String code,

        String semanticVersion,

        String title,

        String summary,

        String content,

        String contentFormat,

        BigDecimal confidence,

        String sourceReference,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeContextType contextType,

        String contextReference,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        LocalDateTime versionCreatedAt
) {

    public KnowledgeSnapshot {
        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        validatePositiveId(
                knowledgeVersionId,
                "El identificador de KnowledgeObjectVersion debe ser válido"
        );

        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        code = normalizeRequired(
                code,
                "El código de conocimiento es obligatorio"
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

        content = normalizeRequired(
                content,
                "El contenido es obligatorio"
        );

        contentFormat = normalizeRequired(
                contentFormat,
                "El formato del contenido es obligatorio"
        );

        confidence = Objects.requireNonNull(
                confidence,
                "El nivel de confianza es obligatorio"
        );

        validateConfidence(confidence);

        sourceReference =
                normalizeOptional(sourceReference);

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

        contextType = Objects.requireNonNull(
                contextType,
                "KnowledgeContextType es obligatorio"
        );

        contextReference = normalizeRequired(
                contextReference,
                "La referencia del contexto es obligatoria"
        );

        validFrom = Objects.requireNonNull(
                validFrom,
                "La fecha inicial de vigencia es obligatoria"
        );

        if (validUntil != null
                && !validUntil.isAfter(validFrom)) {

            throw new IllegalArgumentException(
                    "La fecha final de vigencia debe ser posterior "
                            + "a la fecha inicial"
            );
        }

        versionCreatedAt = Objects.requireNonNull(
                versionCreatedAt,
                "La fecha de creación de la versión es obligatoria"
        );
    }

    /**
     * Determina si este snapshot es aplicable en el momento indicado.
     */
    public boolean isValidAt(LocalDateTime moment) {
        Objects.requireNonNull(
                moment,
                "El momento de consulta es obligatorio"
        );

        boolean alreadyStarted =
                !moment.isBefore(validFrom);

        boolean notExpired =
                validUntil == null
                        || moment.isBefore(validUntil);

        return alreadyStarted && notExpired;
    }

    public boolean hasMinimumConfidence(
            BigDecimal minimum
    ) {
        Objects.requireNonNull(
                minimum,
                "La confianza mínima es obligatoria"
        );

        validateConfidence(minimum);

        return confidence.compareTo(minimum) >= 0;
    }

    public boolean belongsToContext(
            KnowledgeContextType type,
            String reference
    ) {
        if (type == null
                || reference == null
                || reference.isBlank()) {

            return false;
        }

        return contextType == type
                && contextReference.equalsIgnoreCase(
                        reference.trim()
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

    private static void validateConfidence(
            BigDecimal confidence
    ) {
        if (confidence.compareTo(BigDecimal.ZERO) < 0
                || confidence.compareTo(BigDecimal.ONE) > 0) {

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
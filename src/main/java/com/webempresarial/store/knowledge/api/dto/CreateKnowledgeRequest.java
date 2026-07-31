package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateKnowledgeRequest(

        @NotBlank(
                message = "El código del conocimiento es obligatorio"
        )
        @Size(
                max = 100,
                message = "El código no puede superar 100 caracteres"
        )
        String code,

        @NotNull(
                message = "El tipo de conocimiento es obligatorio"
        )
        KnowledgeTypeCode typeCode,

        @NotNull(
                message = "El dominio de conocimiento es obligatorio"
        )
        KnowledgeDomain domain,

        @NotNull(
                message = "La clasificación es obligatoria"
        )
        KnowledgeClassification classification,

        @NotNull(
                message = "El nivel de riesgo es obligatorio"
        )
        KnowledgeRiskLevel riskLevel,

        KnowledgeContextType contextType,

        @Size(
                max = 255,
                message = "La referencia de contexto no puede superar 255 caracteres"
        )
        String contextReference,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        @NotBlank(
                message = "El título de la versión inicial es obligatorio"
        )
        @Size(
                max = 255,
                message = "El título no puede superar 255 caracteres"
        )
        String title,

        @Size(
                max = 2000,
                message = "El resumen no puede superar 2000 caracteres"
        )
        String summary,

        @NotBlank(
                message = "El contenido de la versión inicial es obligatorio"
        )
        String content,

        @NotBlank(
                message = "El formato del contenido es obligatorio"
        )
        @Size(
                max = 50,
                message = "El formato del contenido no puede superar 50 caracteres"
        )
        String contentFormat,

        @NotNull(
                message = "La confianza del conocimiento es obligatoria"
        )
        @DecimalMin(
                value = "0.00",
                message = "La confianza no puede ser menor que 0.00"
        )
        @DecimalMax(
                value = "1.00",
                message = "La confianza no puede ser mayor que 1.00"
        )
        BigDecimal confidence,

        @Size(
                max = 1000,
                message = "La referencia de fuente no puede superar 1000 caracteres"
        )
        String sourceReference
) {

    @AssertTrue(
            message = "El tipo y la referencia de contexto deben proporcionarse juntos"
    )
    public boolean isContextValid() {
        boolean hasType =
                contextType != null;

        boolean hasReference =
                contextReference != null
                        && !contextReference.isBlank();

        return hasType == hasReference;
    }

    @AssertTrue(
            message = "La fecha final de vigencia debe ser posterior a la fecha inicial"
    )
    public boolean isValidityPeriodValid() {
        if (validFrom == null || validUntil == null) {
            return true;
        }

        return validUntil.isAfter(validFrom);
    }

    public String normalizedCode() {
        if (code == null) {
            return null;
        }

        return code
                .trim()
                .toUpperCase();
    }

    public String normalizedContextReference() {
        return normalizeNullable(
                contextReference
        );
    }

    public String normalizedTitle() {
        return normalizeNullable(
                title
        );
    }

    public String normalizedSummary() {
        return normalizeNullable(
                summary
        );
    }

    public String normalizedContentFormat() {
        if (contentFormat == null) {
            return null;
        }

        return contentFormat
                .trim()
                .toUpperCase();
    }

    public String normalizedSourceReference() {
        return normalizeNullable(
                sourceReference
        );
    }

    private String normalizeNullable(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
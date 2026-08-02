package com.webempresarial.store.knowledge.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateKnowledgeVersionRequest(

        @Min(
                value = 0,
                message = "La versión major no puede ser negativa"
        )
        int major,

        @Min(
                value = 0,
                message = "La versión minor no puede ser negativa"
        )
        int minor,

        @Min(
                value = 0,
                message = "La versión patch no puede ser negativa"
        )
        int patch,

        @NotBlank(
                message = "El título es obligatorio"
        )
        @Size(
                max = 200,
                message = "El título no puede superar 200 caracteres"
        )
        String title,

        @NotBlank(
                message = "El resumen es obligatorio"
        )
        @Size(
                max = 1000,
                message = "El resumen no puede superar 1000 caracteres"
        )
        String summary,

        @NotBlank(
                message = "El contenido es obligatorio"
        )
        String content,

        @NotBlank(
                message = "El formato del contenido es obligatorio"
        )
        @Size(
                max = 50,
                message = "El formato no puede superar 50 caracteres"
        )
        String contentFormat,

        @NotNull(
                message = "La confianza es obligatoria"
        )
        @DecimalMin(
                value = "0.0",
                message = "La confianza no puede ser menor que 0"
        )
        @DecimalMax(
                value = "1.0",
                message = "La confianza no puede ser mayor que 1"
        )
        BigDecimal confidence,

        @Size(
                max = 500,
                message = "La referencia de fuente no puede superar 500 caracteres"
        )
        String sourceReference
) {

    public String normalizedTitle() {
        return normalizeRequired(title);
    }

    public String normalizedSummary() {
        return normalizeRequired(summary);
    }

    public String normalizedContent() {
        return normalizeRequired(content);
    }

    public String normalizedContentFormat() {
        return normalizeRequired(contentFormat)
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');
    }

    public String normalizedSourceReference() {
        return normalizeOptional(sourceReference);
    }

    private static String normalizeRequired(
            String value
    ) {
        return value == null
                ? null
                : value.trim();
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
package com.webempresarial.store.knowledge.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record PublishKnowledgeRequest(

        @NotNull(
                message = "El identificador de versión es obligatorio"
        )
        @Positive(
                message = "El identificador de versión debe ser mayor que cero"
        )
        Long versionId,

        @NotNull(
                message = "La fecha inicial de vigencia es obligatoria"
        )
        LocalDateTime validFrom,

        LocalDateTime validUntil
) {

    @AssertTrue(
            message = "La fecha final de vigencia debe ser posterior a la fecha inicial"
    )
    public boolean isValidityPeriodValid() {
        if (validFrom == null || validUntil == null) {
            return true;
        }

        return validUntil.isAfter(validFrom);
    }
}
package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;

import java.math.BigDecimal;

public record KnowledgeDomainMetrics(

        KnowledgeDomain domain,

        long objectCount,

        BigDecimal percentage
) {

    public KnowledgeDomainMetrics {
        if (domain == null) {
            throw new IllegalArgumentException(
                    "El dominio de conocimiento es obligatorio"
            );
        }

        if (objectCount < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de objetos no puede ser negativa"
            );
        }

        if (percentage != null
                && (
                    percentage.compareTo(
                            BigDecimal.ZERO
                    ) < 0
                    || percentage.compareTo(
                            new BigDecimal("100")
                    ) > 0
                )) {

            throw new IllegalArgumentException(
                    "El porcentaje debe estar entre 0 y 100"
            );
        }
    }
}
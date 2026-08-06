package com.webempresarial.store.knowledge.api.dto;

import java.math.BigDecimal;

public record KnowledgeDashboardMetrics(

        long totalObjects,

        long draftObjects,

        long reviewObjects,

        long approvedObjects,

        long publishedObjects,

        long archivedObjects,

        long totalVersions,

        BigDecimal averageConfidence
) {

    public KnowledgeDashboardMetrics {
        if (totalObjects < 0) {
            throw new IllegalArgumentException(
                    "El total de objetos no puede ser negativo"
            );
        }

        if (draftObjects < 0
                || reviewObjects < 0
                || approvedObjects < 0
                || publishedObjects < 0
                || archivedObjects < 0) {

            throw new IllegalArgumentException(
                    "Las métricas por estado no pueden ser negativas"
            );
        }

        if (totalVersions < 0) {
            throw new IllegalArgumentException(
                    "El total de versiones no puede ser negativo"
            );
        }

        if (averageConfidence != null
                && (
                    averageConfidence.compareTo(
                            BigDecimal.ZERO
                    ) < 0
                    || averageConfidence.compareTo(
                            BigDecimal.ONE
                    ) > 0
                )) {

            throw new IllegalArgumentException(
                    "La confianza promedio debe estar entre 0 y 1"
            );
        }
    }

    public static KnowledgeDashboardMetrics empty() {
        return new KnowledgeDashboardMetrics(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null
        );
    }
}
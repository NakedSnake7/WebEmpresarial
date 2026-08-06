package com.webempresarial.store.knowledge.infrastructure.dashboard;

import java.math.BigDecimal;

public record KnowledgeDashboardMetricsSnapshot(

        long totalObjects,

        long draftObjects,

        long reviewObjects,

        long approvedObjects,

        long publishedObjects,

        long archivedObjects,

        long totalVersions,

        BigDecimal averageConfidence
) {

    public KnowledgeDashboardMetricsSnapshot {
        validateNonNegative(
                totalObjects,
                "totalObjects"
        );

        validateNonNegative(
                draftObjects,
                "draftObjects"
        );

        validateNonNegative(
                reviewObjects,
                "reviewObjects"
        );

        validateNonNegative(
                approvedObjects,
                "approvedObjects"
        );

        validateNonNegative(
                publishedObjects,
                "publishedObjects"
        );

        validateNonNegative(
                archivedObjects,
                "archivedObjects"
        );

        validateNonNegative(
                totalVersions,
                "totalVersions"
        );

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

    private static void validateNonNegative(
            long value,
            String field
    ) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    field + " no puede ser negativo"
            );
        }
    }
}
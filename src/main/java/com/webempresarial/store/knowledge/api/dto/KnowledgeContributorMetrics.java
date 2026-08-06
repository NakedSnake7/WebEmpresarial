package com.webempresarial.store.knowledge.api.dto;

public record KnowledgeContributorMetrics(

        String actor,

        long knowledgeObjectCount,

        long versionCount,

        long totalContributions
) {

    public KnowledgeContributorMetrics {
        actor = normalizeActor(
                actor
        );

        if (knowledgeObjectCount < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de objetos no puede ser negativa"
            );
        }

        if (versionCount < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de versiones no puede ser negativa"
            );
        }

        if (totalContributions < 0) {
            throw new IllegalArgumentException(
                    "El total de contribuciones no puede ser negativo"
            );
        }
    }

    public static KnowledgeContributorMetrics of(
            String actor,
            long knowledgeObjectCount,
            long versionCount
    ) {
        return new KnowledgeContributorMetrics(
                actor,
                knowledgeObjectCount,
                versionCount,
                knowledgeObjectCount + versionCount
        );
    }

    private static String normalizeActor(
            String actor
    ) {
        if (actor == null || actor.isBlank()) {
            return "SYSTEM";
        }

        return actor.trim();
    }
}
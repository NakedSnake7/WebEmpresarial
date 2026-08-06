package com.webempresarial.store.knowledge.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record KnowledgeDashboardResponse(

        KnowledgeDashboardMetrics metrics,

        List<KnowledgeActivityResponse> recentActivity,

        List<KnowledgeDomainMetrics> topDomains,

        List<KnowledgeContributorMetrics> topContributors,

        LocalDateTime generatedAt
) {

    public KnowledgeDashboardResponse {
        metrics =
                metrics != null
                        ? metrics
                        : KnowledgeDashboardMetrics.empty();

        recentActivity =
                recentActivity == null
                        ? List.of()
                        : List.copyOf(
                                recentActivity
                        );

        topDomains =
                topDomains == null
                        ? List.of()
                        : List.copyOf(
                                topDomains
                        );

        topContributors =
                topContributors == null
                        ? List.of()
                        : List.copyOf(
                                topContributors
                        );

        generatedAt =
                generatedAt != null
                        ? generatedAt
                        : LocalDateTime.now();
    }

    public static KnowledgeDashboardResponse empty() {
        return new KnowledgeDashboardResponse(
                KnowledgeDashboardMetrics.empty(),
                List.of(),
                List.of(),
                List.of(),
                LocalDateTime.now()
        );
    }
}
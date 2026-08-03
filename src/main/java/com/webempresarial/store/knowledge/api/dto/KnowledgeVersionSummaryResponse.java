package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KnowledgeVersionSummaryResponse(

        Long id,

        String semanticVersion,

        String title,

        String summary,

        String contentFormat,

        BigDecimal confidence,

        String sourceReference,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        String createdBy,

        boolean current,

        boolean latest
) {

    public static KnowledgeVersionSummaryResponse from(
            KnowledgeObjectVersion version,
            Long currentVersionId,
            Long latestVersionId
    ) {
        if (version == null) {
            throw new IllegalArgumentException(
                    "KnowledgeObjectVersion es obligatoria"
            );
        }

        Long versionId =
                version.getId();

        return new KnowledgeVersionSummaryResponse(
                versionId,

                version.getSemanticVersion() != null
                        ? version.getSemanticVersion().toString()
                        : null,

                version.getTitle(),
                version.getSummary(),
                version.getContentFormat(),

                version.getConfidence() != null
                        ? version.getConfidence().getValue()
                        : null,

                version.getSourceReference(),
                version.getCreatedAt(),
                version.getUpdatedAt(),
                version.getCreatedBy(),

                versionId != null
                        && versionId.equals(currentVersionId),

                versionId != null
                        && versionId.equals(latestVersionId)
        );
    }
}
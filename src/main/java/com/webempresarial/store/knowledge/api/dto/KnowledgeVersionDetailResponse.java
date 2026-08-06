package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KnowledgeVersionDetailResponse(

        Long id,

        Long knowledgeObjectId,

        String semanticVersion,

        String title,

        String summary,

        String content,

        String contentFormat,

        String renderedContent,

        String renderedContentFormat,

        BigDecimal confidence,

        String sourceReference,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        String createdBy,

        String updatedBy,

        boolean current,

        boolean latest
) {

    public static KnowledgeVersionDetailResponse from(
            KnowledgeObjectVersion version,
            String renderedContent,
            String renderedContentFormat,
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

        return new KnowledgeVersionDetailResponse(
                versionId,

                version.getKnowledgeObject() != null
                        ? version.getKnowledgeObject().getId()
                        : null,

                version.getSemanticVersion() != null
                        ? version.getSemanticVersion().toString()
                        : null,

                version.getTitle(),
                version.getSummary(),
                version.getContent(),
                version.getContentFormat(),
                renderedContent,
                renderedContentFormat,

                version.getConfidence() != null
                        ? version.getConfidence().getValue()
                        : null,

                version.getSourceReference(),
                version.getCreatedAt(),
                version.getUpdatedAt(),
                version.getCreatedBy(),
                version.getUpdatedBy(),

                versionId != null
                        && versionId.equals(currentVersionId),

                versionId != null
                        && versionId.equals(latestVersionId)
        );
    }
}
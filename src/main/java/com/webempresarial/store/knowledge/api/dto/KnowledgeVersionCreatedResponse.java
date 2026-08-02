package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KnowledgeVersionCreatedResponse(

        Long knowledgeObjectId,

        Long versionId,

        String semanticVersion,

        String title,

        BigDecimal confidence,

        LocalDateTime createdAt
) {

    public static KnowledgeVersionCreatedResponse from(
            KnowledgeObjectVersion version
    ) {
        if (version == null) {
            throw new IllegalArgumentException(
                    "KnowledgeObjectVersion es obligatoria"
            );
        }

        return new KnowledgeVersionCreatedResponse(
                version.getKnowledgeObject().getId(),
                version.getId(),
                version.getSemanticVersion().toString(),
                version.getTitle(),
                version.getConfidence().getValue(),
                version.getCreatedAt()
        );
    }
}
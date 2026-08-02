package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;

import java.time.LocalDateTime;

public record KnowledgeLifecycleResponse(

        Long id,
        String code,
        KnowledgeStatus status,
        Long currentVersionId,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        LocalDateTime updatedAt
) {

    public static KnowledgeLifecycleResponse from(
            KnowledgeObject knowledgeObject
    ) {
        if (knowledgeObject == null) {
            throw new IllegalArgumentException(
                    "KnowledgeObject es obligatorio"
            );
        }

        return new KnowledgeLifecycleResponse(
                knowledgeObject.getId(),
                knowledgeObject.getCode().getValue(),
                knowledgeObject.getStatus(),

                knowledgeObject.getCurrentVersion() != null
                        ? knowledgeObject
                                .getCurrentVersion()
                                .getId()
                        : null,

                knowledgeObject.getValidFrom(),
                knowledgeObject.getValidUntil(),
                knowledgeObject.getUpdatedAt()
        );
    }
}
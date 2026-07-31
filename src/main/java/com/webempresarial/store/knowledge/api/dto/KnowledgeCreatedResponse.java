package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;

import java.time.LocalDateTime;

public record KnowledgeCreatedResponse(

        Long id,

        String code,

        KnowledgeStatus status,

        Long currentVersionId,

        String semanticVersion,

        LocalDateTime createdAt
) {

    public static KnowledgeCreatedResponse from(
            KnowledgeObject knowledgeObject
    ) {
        if (knowledgeObject == null) {
            throw new IllegalArgumentException(
                    "KnowledgeObject es obligatorio"
            );
        }

        KnowledgeObjectVersion currentVersion =
                knowledgeObject.getCurrentVersion();

        return new KnowledgeCreatedResponse(
                knowledgeObject.getId(),

                knowledgeObject.getCode() != null
                        ? knowledgeObject.getCode().getValue()
                        : null,

                knowledgeObject.getStatus(),

                currentVersion != null
                        ? currentVersion.getId()
                        : null,

                currentVersion != null
                        && currentVersion.getSemanticVersion() != null
                        ? currentVersion
                                .getSemanticVersion()
                                .toString()
                        : null,

                knowledgeObject.getCreatedAt()
        );
    }
}
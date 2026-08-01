package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;

import java.time.LocalDateTime;

public record KnowledgeCreatedResponse(

        Long id,

        String code,

        KnowledgeStatus status,

        Long initialVersionId,

        String semanticVersion,

        LocalDateTime createdAt
) {

    public static KnowledgeCreatedResponse from(
            KnowledgeObject knowledgeObject,
            KnowledgeObjectVersion initialVersion
    ) {
        if (knowledgeObject == null) {
            throw new IllegalArgumentException(
                    "KnowledgeObject es obligatorio"
            );
        }

        if (initialVersion == null) {
            throw new IllegalArgumentException(
                    "La versión inicial es obligatoria"
            );
        }

        return new KnowledgeCreatedResponse(
                knowledgeObject.getId(),
                knowledgeObject.getCode().getValue(),
                knowledgeObject.getStatus(),
                initialVersion.getId(),
                initialVersion.getSemanticVersion().toString(),
                knowledgeObject.getCreatedAt()
        );
    }
}
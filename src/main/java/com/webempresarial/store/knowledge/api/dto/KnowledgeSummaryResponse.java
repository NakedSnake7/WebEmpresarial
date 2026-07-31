package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KnowledgeSummaryResponse(

        Long id,

        String code,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeStatus status,

        KnowledgeContextType contextType,

        String contextReference,

        Long currentVersionId,

        String semanticVersion,

        String title,

        String summary,

        BigDecimal confidence,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {

    public static KnowledgeSummaryResponse from(
            KnowledgeObject knowledgeObject
    ) {
        KnowledgeObjectVersion currentVersion =
                knowledgeObject.getCurrentVersion();

        return new KnowledgeSummaryResponse(
                knowledgeObject.getId(),
                knowledgeObject.getCode().getValue(),
                knowledgeObject.getTypeCode(),
                knowledgeObject.getDomain(),
                knowledgeObject.getClassification(),
                knowledgeObject.getRiskLevel(),
                knowledgeObject.getStatus(),

                knowledgeObject.getContextRoot() != null
                        ? knowledgeObject
                                .getContextRoot()
                                .getType()
                        : null,

                knowledgeObject.getContextRoot() != null
                        ? knowledgeObject
                                .getContextRoot()
                                .getReference()
                        : null,

                currentVersion != null
                        ? currentVersion.getId()
                        : null,

                currentVersion != null
                        ? currentVersion
                                .getSemanticVersion()
                                .toString()
                        : null,

                currentVersion != null
                        ? currentVersion.getTitle()
                        : null,

                currentVersion != null
                        ? currentVersion.getSummary()
                        : null,

                currentVersion != null
                        && currentVersion.getConfidence() != null
                        ? currentVersion
                                .getConfidence()
                                .getValue()
                        : null,

                knowledgeObject.getValidFrom(),
                knowledgeObject.getValidUntil(),
                knowledgeObject.getCreatedAt(),
                knowledgeObject.getUpdatedAt()
        );
    }
}
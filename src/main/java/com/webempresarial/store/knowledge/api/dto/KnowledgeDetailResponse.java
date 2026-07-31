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

public record KnowledgeDetailResponse(

        Long id,

        String code,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeStatus status,

        KnowledgeContextType contextType,

        String contextReference,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        VersionDetail currentVersion
) {

    public static KnowledgeDetailResponse from(
            KnowledgeObject knowledgeObject
    ) {
        if (knowledgeObject == null) {
            throw new IllegalArgumentException(
                    "KnowledgeObject es obligatorio"
            );
        }

        return new KnowledgeDetailResponse(
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

                knowledgeObject.getValidFrom(),
                knowledgeObject.getValidUntil(),
                knowledgeObject.getCreatedAt(),
                knowledgeObject.getUpdatedAt(),

                VersionDetail.from(
                        knowledgeObject.getCurrentVersion()
                )
        );
    }

    public record VersionDetail(

            Long id,

            String semanticVersion,

            String title,

            String summary,

            String content,

            String contentFormat,

            BigDecimal confidence,

            String sourceReference,

            LocalDateTime createdAt,

            LocalDateTime updatedAt,

            String createdBy,

            String updatedBy
    ) {

        public static VersionDetail from(
                KnowledgeObjectVersion version
        ) {
            if (version == null) {
                return null;
            }

            return new VersionDetail(
                    version.getId(),

                    version.getSemanticVersion() != null
                            ? version
                                    .getSemanticVersion()
                                    .toString()
                            : null,

                    version.getTitle(),
                    version.getSummary(),
                    version.getContent(),
                    version.getContentFormat(),

                    version.getConfidence() != null
                            ? version
                                    .getConfidence()
                                    .getValue()
                            : null,

                    version.getSourceReference(),
                    version.getCreatedAt(),
                    version.getUpdatedAt(),
                    version.getCreatedBy(),
                    version.getUpdatedBy()
            );
        }
    }
}
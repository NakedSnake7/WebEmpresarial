package com.webempresarial.store.knowledge.api.dto;

import com.webempresarial.store.knowledge.application.query.KnowledgeQueryCriteria;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KnowledgeSearchRequest(

        @Size(max = 100)
        String code,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeStatus status,

        KnowledgeContextType contextType,

        @Size(max = 150)
        String contextReference,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        BigDecimal minimumConfidence,

        LocalDateTime effectiveAt,

        @Size(max = 250)
        String text,

        @Min(0)
        Integer page,

        @Min(1)
        @Max(100)
        Integer size
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public KnowledgeQueryCriteria toCriteria(
            Long storeId
    ) {
        KnowledgeQueryCriteria.Builder builder =
                KnowledgeQueryCriteria.builder(storeId);

        if (hasText(code)) {
            builder.code(code.trim());
        }

        if (typeCode != null) {
            builder.typeCode(typeCode);
        }

        if (domain != null) {
            builder.domain(domain);
        }

        if (classification != null) {
            builder.classification(classification);
        }

        if (riskLevel != null) {
            builder.riskLevel(riskLevel);
        }

        if (status != null) {
            builder.status(status);
        }

        if (contextType != null || hasText(contextReference)) {
            builder.context(
                    contextType,
                    normalize(contextReference)
            );
        }

        if (minimumConfidence != null) {
            builder.minimumConfidence(
                    minimumConfidence
            );
        }

        if (effectiveAt != null) {
            builder.effectiveAt(effectiveAt);
        }

        if (hasText(text)) {
            builder.text(text.trim());
        }

        builder.page(
                page != null
                        ? page
                        : DEFAULT_PAGE
        );

        builder.size(
                size != null
                        ? size
                        : DEFAULT_SIZE
        );

        return builder.build();
    }

    private static boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }

    private static String normalize(
            String value
    ) {
        return hasText(value)
                ? value.trim()
                : null;
    }
}
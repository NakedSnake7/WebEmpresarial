package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;

import java.time.LocalDateTime;

final class KnowledgeQueryTestFixtures {

    private KnowledgeQueryTestFixtures() {
    }

    static KnowledgeQueryItem draftItem() {
        return new KnowledgeQueryItem(
                100L,
                15L,
                "KS-100",
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                KnowledgeStatus.DRAFT,
                KnowledgeContextType.PROJECT,
                "ROBERT-SLINGERLAND",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        20,
                        0
                ),
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        20,
                        0
                )
        );
    }

    private static KnowledgeTypeCode firstTypeCode() {
        return KnowledgeTypeCode.values()[0];
    }

    private static KnowledgeDomain firstDomain() {
        return KnowledgeDomain.values()[0];
    }

    private static KnowledgeClassification firstClassification() {
        return KnowledgeClassification.values()[0];
    }

    private static KnowledgeRiskLevel firstRiskLevel() {
        return KnowledgeRiskLevel.values()[0];
    }
}
package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateKnowledgeObjectResultTest {

    @Test
    void shouldCreateResult() {
        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        20,
                        45
                );

        CreateKnowledgeObjectResult result =
                new CreateKnowledgeObjectResult(
                        100L,
                        15L,
                        " KS-100 ",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeStatus.DRAFT,
                        KnowledgeContextType.PROJECT,
                        " ROBERT-SLINGERLAND ",
                        " admin@webempresarial.com ",
                        createdAt,
                        0L
                );

        assertEquals(100L, result.id());
        assertEquals(15L, result.storeId());
        assertEquals("KS-100", result.code());

        assertEquals(
                KnowledgeStatus.DRAFT,
                result.status()
        );

        assertEquals(
                KnowledgeContextType.PROJECT,
                result.contextType()
        );

        assertEquals(
                "ROBERT-SLINGERLAND",
                result.contextReference()
        );

        assertEquals(
                "admin@webempresarial.com",
                result.createdBy()
        );

        assertEquals(createdAt, result.createdAt());
        assertEquals(0L, result.lockVersion());
    }

    @Test
    void shouldRejectInvalidKnowledgeObjectId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validResult(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validResult(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validResult(-1L)
        );
    }

    @Test
    void shouldRejectInvalidStoreId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeObjectResult(
                        1L,
                        null,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeStatus.DRAFT,
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        "admin",
                        LocalDateTime.now(),
                        0L
                )
        );
    }

    @Test
    void shouldRejectBlankCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeObjectResult(
                        1L,
                        1L,
                        " ",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeStatus.DRAFT,
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        "admin",
                        LocalDateTime.now(),
                        0L
                )
        );
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeObjectResult(
                        1L,
                        1L,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        null,
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        "admin",
                        LocalDateTime.now(),
                        0L
                )
        );
    }

    @Test
    void shouldRejectNullCreationDate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeObjectResult(
                        1L,
                        1L,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeStatus.DRAFT,
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        "admin",
                        null,
                        0L
                )
        );
    }

    @Test
    void shouldRejectInvalidLockVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeObjectResult(
                        1L,
                        1L,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeStatus.DRAFT,
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        "admin",
                        LocalDateTime.now(),
                        -1L
                )
        );
    }

    private static CreateKnowledgeObjectResult validResult(
            Long id
    ) {
        return new CreateKnowledgeObjectResult(
                id,
                1L,
                "KS-100",
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                KnowledgeStatus.DRAFT,
                KnowledgeContextType.PROJECT,
                "PROJECT-1",
                "admin",
                LocalDateTime.now(),
                0L
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
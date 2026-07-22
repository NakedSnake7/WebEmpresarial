package com.webempresarial.store.knowledge.application.command;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateKnowledgeObjectCommandTest {

    /*
     * Sustituye los valores usados abajo si tus enums no contienen
     * exactamente estas constantes.
     */

    @Test
    void shouldCreateCommand() {
        CreateKnowledgeObjectCommand command =
                new CreateKnowledgeObjectCommand(
                        15L,
                        " KS-100 ",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeContextType.PROJECT,
                        " ROBERT-SLINGERLAND ",
                        " admin@webempresarial.com "
                );

        assertEquals(15L, command.storeId());
        assertEquals("KS-100", command.code());
        assertEquals(
                "ROBERT-SLINGERLAND",
                command.contextReference()
        );
        assertEquals(
                "admin@webempresarial.com",
                command.actor()
        );
    }

    @Test
    void shouldRejectInvalidStoreId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(-1L)
        );
    }

    @Test
    void shouldRejectBlankCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeObjectCommand(
                        1L,
                        " ",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectMissingEnums() {
        assertThrows(
                NullPointerException.class,
                () -> new CreateKnowledgeObjectCommand(
                        1L,
                        "KS-100",
                        null,
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        "admin"
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> new CreateKnowledgeObjectCommand(
                        1L,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        null,
                        "PROJECT-1",
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectBlankContextReference() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeObjectCommand(
                        1L,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeContextType.PROJECT,
                        " ",
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectBlankActor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeObjectCommand(
                        1L,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        " "
                )
        );
    }

    private static CreateKnowledgeObjectCommand validCommand(
            Long storeId
    ) {
        return new CreateKnowledgeObjectCommand(
                storeId,
                "KS-100",
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                KnowledgeContextType.PROJECT,
                "PROJECT-1",
                "admin"
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
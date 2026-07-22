package com.webempresarial.store.knowledge.domain.value;

import org.junit.jupiter.api.Test; 

import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeContextRootTest {

    @Test
    void shouldCreatePlatformContext() {
        KnowledgeContextRoot context =
                KnowledgeContextRoot.platform();

        assertEquals(
                KnowledgeContextType.PLATFORM,
                context.getType()
        );

        assertEquals(
                "WEBEMPRESARIAL",
                context.getReference()
        );

        assertEquals(
                "PLATFORM:WEBEMPRESARIAL",
                context.toString()
        );
    }

    @Test
    void shouldCreateStoreContext() {
        KnowledgeContextRoot context =
                KnowledgeContextRoot.store(15L);

        assertEquals(
                KnowledgeContextType.STORE,
                context.getType()
        );

        assertEquals(
                "15",
                context.getReference()
        );

        assertTrue(context.belongsToStore(15L));
        assertFalse(context.belongsToStore(16L));
    }

    @Test
    void shouldNormalizeCustomReference() {
        KnowledgeContextRoot context =
                KnowledgeContextRoot.of(
                        KnowledgeContextType.PROJECT,
                        "  Robert Slingerland / Website  "
                );

        assertEquals(
                "ROBERT-SLINGERLAND-WEBSITE",
                context.getReference()
        );
    }

    @Test
    void shouldRejectBlankReference() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeContextRoot.of(
                        KnowledgeContextType.PROJECT,
                        " "
                )
        );
    }

    @Test
    void shouldRejectInvalidStoreId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeContextRoot.store(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeContextRoot.store(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeContextRoot.store(-1L)
        );
    }

    @Test
    void shouldCompareByValue() {
        KnowledgeContextRoot first =
                KnowledgeContextRoot.of(
                        KnowledgeContextType.SYSTEM,
                        "commerce"
                );

        KnowledgeContextRoot second =
                KnowledgeContextRoot.of(
                        KnowledgeContextType.SYSTEM,
                        "COMMERCE"
                );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
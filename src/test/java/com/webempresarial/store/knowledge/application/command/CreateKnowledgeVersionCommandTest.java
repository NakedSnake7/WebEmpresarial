package com.webempresarial.store.knowledge.application.command;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateKnowledgeVersionCommandTest {

    @Test
    void shouldCreateCommand() {
        CreateKnowledgeVersionCommand command =
                new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        " 1.0.0 ",
                        " Knowledge Objects Specification ",
                        " Especificación del modelo de conocimiento ",
                        " # Knowledge Objects\nContenido principal ",
                        " markdown ",
                        new BigDecimal("0.9500"),
                        " https://example.com/source ",
                        " admin@webempresarial.com "
                );

        assertEquals(15L, command.storeId());
        assertEquals(100L, command.knowledgeObjectId());
        assertEquals("1.0.0", command.semanticVersion());

        assertEquals(
                "Knowledge Objects Specification",
                command.title()
        );

        assertEquals(
                "Especificación del modelo de conocimiento",
                command.summary()
        );

        assertEquals(
                "# Knowledge Objects\nContenido principal",
                command.content()
        );

        assertEquals(
                "markdown",
                command.contentFormat()
        );

        assertEquals(
                new BigDecimal("0.9500"),
                command.confidence()
        );

        assertEquals(
                "https://example.com/source",
                command.sourceReference()
        );

        assertEquals(
                "admin@webempresarial.com",
                command.actor()
        );
    }

    @Test
    void shouldAllowMissingSourceReference() {
        CreateKnowledgeVersionCommand command =
                new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        "1.0.0",
                        "Title",
                        "Summary",
                        "Content",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        " ",
                        "admin"
                );

        assertNull(command.sourceReference());
    }

    @Test
    void shouldRejectInvalidStoreId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(null, 100L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(0L, 100L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(-1L, 100L)
        );
    }

    @Test
    void shouldRejectInvalidKnowledgeObjectId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(15L, null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(15L, 0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validCommand(15L, -1L)
        );
    }

    @Test
    void shouldRejectBlankSemanticVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        " ",
                        "Title",
                        "Summary",
                        "Content",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        null,
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        "1.0.0",
                        " ",
                        "Summary",
                        "Content",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        null,
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectBlankSummary() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        "1.0.0",
                        "Title",
                        " ",
                        "Content",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        null,
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectBlankContent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        "1.0.0",
                        "Title",
                        "Summary",
                        " ",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        null,
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectBlankContentFormat() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        "1.0.0",
                        "Title",
                        "Summary",
                        "Content",
                        " ",
                        BigDecimal.ONE,
                        null,
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectMissingConfidence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        "1.0.0",
                        "Title",
                        "Summary",
                        "Content",
                        "MARKDOWN",
                        null,
                        null,
                        "admin"
                )
        );
    }

    @Test
    void shouldRejectBlankActor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        "1.0.0",
                        "Title",
                        "Summary",
                        "Content",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        null,
                        " "
                )
        );
    }

    @Test
    void shouldRejectTitleLongerThanMaximum() {
        String title =
                "A".repeat(
                        CreateKnowledgeVersionCommand
                                .MAX_TITLE_LENGTH
                                + 1
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionCommand(
                        15L,
                        100L,
                        "1.0.0",
                        title,
                        "Summary",
                        "Content",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        null,
                        "admin"
                )
        );
    }

    private static CreateKnowledgeVersionCommand validCommand(
            Long storeId,
            Long knowledgeObjectId
    ) {
        return new CreateKnowledgeVersionCommand(
                storeId,
                knowledgeObjectId,
                "1.0.0",
                "Knowledge Objects Specification",
                "Specification summary",
                "Version content",
                "MARKDOWN",
                new BigDecimal("0.9000"),
                null,
                "admin"
        );
    }
}
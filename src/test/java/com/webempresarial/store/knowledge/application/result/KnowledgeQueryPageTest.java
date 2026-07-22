package com.webempresarial.store.knowledge.application.result;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeQueryPageTest {

    @Test
    void shouldCreateEmptyPage() {
        KnowledgeQueryPage page =
                KnowledgeQueryPage.empty(
                        0,
                        20
                );

        assertTrue(page.isEmpty());
        assertEquals(0, page.numberOfElements());
        assertEquals(0, page.totalElements());
        assertEquals(0, page.totalPages());
        assertTrue(page.first());
        assertTrue(page.last());
        assertFalse(page.hasNext());
        assertFalse(page.hasPrevious());
    }

    @Test
    void shouldCreateFirstPageWithNextPage() {
        KnowledgeQueryItem item =
                KnowledgeQueryTestFixtures.draftItem();

        KnowledgeQueryPage page =
                new KnowledgeQueryPage(
                        List.of(item),
                        0,
                        1,
                        2,
                        2,
                        true,
                        false,
                        true,
                        false
                );

        assertFalse(page.isEmpty());
        assertEquals(1, page.numberOfElements());
        assertTrue(page.hasNext());
        assertFalse(page.hasPrevious());
    }

    @Test
    void shouldRejectInconsistentTotalPages() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeQueryPage(
                        List.of(),
                        0,
                        20,
                        50,
                        1,
                        true,
                        false,
                        true,
                        false
                )
        );
    }

    @Test
    void itemsShouldBeImmutable() {
        KnowledgeQueryItem item =
                KnowledgeQueryTestFixtures.draftItem();

        KnowledgeQueryPage page =
                new KnowledgeQueryPage(
                        List.of(item),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true,
                        false,
                        false
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> page.items().add(item)
        );
    }
}
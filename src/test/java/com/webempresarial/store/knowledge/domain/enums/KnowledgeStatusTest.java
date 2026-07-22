package com.webempresarial.store.knowledge.domain.enums;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeStatusTest {

    @Test
    void shouldReturnDraftAsInitialStatus() {
        assertEquals(
                KnowledgeStatus.DRAFT,
                KnowledgeStatus.initial()
        );
    }

    @Test
    void draftShouldOnlyTransitionToInReview() {
        KnowledgeStatus status =
                KnowledgeStatus.DRAFT;

        assertTrue(
                status.canTransitionTo(
                        KnowledgeStatus.IN_REVIEW
                )
        );

        assertFalse(
                status.canTransitionTo(
                        KnowledgeStatus.APPROVED
                )
        );

        assertFalse(
                status.canTransitionTo(
                        KnowledgeStatus.PUBLISHED
                )
        );
    }

    @Test
    void inReviewShouldTransitionToDraftOrApproved() {
        KnowledgeStatus status =
                KnowledgeStatus.IN_REVIEW;

        assertTrue(
                status.canTransitionTo(
                        KnowledgeStatus.DRAFT
                )
        );

        assertTrue(
                status.canTransitionTo(
                        KnowledgeStatus.APPROVED
                )
        );

        assertFalse(
                status.canTransitionTo(
                        KnowledgeStatus.PUBLISHED
                )
        );
    }

    @Test
    void approvedShouldTransitionToDraftOrPublished() {
        KnowledgeStatus status =
                KnowledgeStatus.APPROVED;

        assertTrue(
                status.canTransitionTo(
                        KnowledgeStatus.DRAFT
                )
        );

        assertTrue(
                status.canTransitionTo(
                        KnowledgeStatus.PUBLISHED
                )
        );

        assertFalse(
                status.canTransitionTo(
                        KnowledgeStatus.ARCHIVED
                )
        );
    }

    @Test
    void publishedShouldOnlyTransitionToArchived() {
        KnowledgeStatus status =
                KnowledgeStatus.PUBLISHED;

        assertTrue(
                status.canTransitionTo(
                        KnowledgeStatus.ARCHIVED
                )
        );

        assertFalse(
                status.canTransitionTo(
                        KnowledgeStatus.DRAFT
                )
        );

        assertFalse(
                status.canTransitionTo(
                        KnowledgeStatus.RETIRED
                )
        );
    }

    @Test
    void archivedShouldTransitionToPublishedOrRetired() {
        KnowledgeStatus status =
                KnowledgeStatus.ARCHIVED;

        assertTrue(
                status.canTransitionTo(
                        KnowledgeStatus.PUBLISHED
                )
        );

        assertTrue(
                status.canTransitionTo(
                        KnowledgeStatus.RETIRED
                )
        );

        assertFalse(
                status.canTransitionTo(
                        KnowledgeStatus.DRAFT
                )
        );
    }

    @Test
    void retiredShouldNotAllowTransitions() {
        KnowledgeStatus status =
                KnowledgeStatus.RETIRED;

        assertTrue(status.isTerminal());
        assertTrue(status.allowedTransitions().isEmpty());

        for (KnowledgeStatus target
                : KnowledgeStatus.values()) {

            assertFalse(
                    status.canTransitionTo(target)
            );
        }
    }

    @Test
    void shouldRejectNullAndSameStatusTransitions() {
        KnowledgeStatus status =
                KnowledgeStatus.DRAFT;

        assertFalse(status.canTransitionTo(null));

        assertFalse(
                status.canTransitionTo(
                        KnowledgeStatus.DRAFT
                )
        );
    }

    @Test
    void shouldValidateAllowedTransition() {
        KnowledgeStatus.DRAFT.validateTransitionTo(
                KnowledgeStatus.IN_REVIEW
        );
    }

    @Test
    void shouldRejectInvalidTransition() {
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> KnowledgeStatus.DRAFT
                                .validateTransitionTo(
                                        KnowledgeStatus.PUBLISHED
                                )
                );

        assertTrue(
                exception.getMessage()
                        .contains("DRAFT")
        );

        assertTrue(
                exception.getMessage()
                        .contains("PUBLISHED")
        );
    }

    @Test
    void shouldReturnAllowedTransitions() {
        Set<KnowledgeStatus> transitions =
                KnowledgeStatus.IN_REVIEW
                        .allowedTransitions();

        assertEquals(2, transitions.size());

        assertTrue(
                transitions.contains(
                        KnowledgeStatus.DRAFT
                )
        );

        assertTrue(
                transitions.contains(
                        KnowledgeStatus.APPROVED
                )
        );
    }

    @Test
    void shouldIdentifyEditableStatuses() {
        assertTrue(
                KnowledgeStatus.DRAFT.isEditable()
        );

        assertTrue(
                KnowledgeStatus.IN_REVIEW.isEditable()
        );

        assertFalse(
                KnowledgeStatus.APPROVED.isEditable()
        );

        assertFalse(
                KnowledgeStatus.PUBLISHED.isEditable()
        );
    }

    @Test
    void shouldIdentifyApprovedStatuses() {
        assertFalse(
                KnowledgeStatus.DRAFT.isApproved()
        );

        assertFalse(
                KnowledgeStatus.IN_REVIEW.isApproved()
        );

        assertTrue(
                KnowledgeStatus.APPROVED.isApproved()
        );

        assertTrue(
                KnowledgeStatus.PUBLISHED.isApproved()
        );

        assertTrue(
                KnowledgeStatus.ARCHIVED.isApproved()
        );

        assertTrue(
                KnowledgeStatus.RETIRED.isApproved()
        );
    }

    @Test
    void shouldIdentifyPublishedStatus() {
        assertTrue(
                KnowledgeStatus.PUBLISHED.isPublished()
        );

        assertFalse(
                KnowledgeStatus.APPROVED.isPublished()
        );

        assertFalse(
                KnowledgeStatus.ARCHIVED.isPublished()
        );
    }
}
package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultReviewStrategicSynthesisCommandTest {

    @Mock
    private StrategicSynthesisStore synthesisStore;

    @Mock
    private ReviewStrategicSynthesisService reviewService;

    private DefaultReviewStrategicSynthesisCommand command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        command =
                new DefaultReviewStrategicSynthesisCommand(
                        synthesisStore,
                        reviewService
                );
    }

    @Test
    void shouldApproveStoredSynthesis() {
        StoredStrategicSynthesis stored =
                mockStoredSynthesis(
                        42L
                );

        ReviewStrategicSynthesisResult reviewResult =
                mock(
                        ReviewStrategicSynthesisResult.class
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        42L
                )
        ).thenReturn(
                Optional.of(
                        stored
                )
        );

        when(
                reviewService.review(
                        stored,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis está correctamente sustentada"
                )
        ).thenReturn(
                reviewResult
        );

        ReviewStrategicSynthesisResult result =
                command.review(
                        10L,
                        20L,
                        42L,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis está correctamente sustentada"
                );

        assertThat(result)
                .isSameAs(
                        reviewResult
                );

        verify(synthesisStore)
                .findSnapshot(
                        10L,
                        20L,
                        42L
                );

        verify(reviewService)
                .review(
                        stored,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis está correctamente sustentada"
                );
    }

    @Test
    void shouldRejectStoredSynthesis() {
        StoredStrategicSynthesis stored =
                mockStoredSynthesis(
                        42L
                );

        ReviewStrategicSynthesisResult reviewResult =
                mock(
                        ReviewStrategicSynthesisResult.class
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        42L
                )
        ).thenReturn(
                Optional.of(
                        stored
                )
        );

        when(
                reviewService.review(
                        stored,
                        "reviewer@example.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La evidencia no es suficiente"
                )
        ).thenReturn(
                reviewResult
        );

        ReviewStrategicSynthesisResult result =
                command.review(
                        10L,
                        20L,
                        42L,
                        "reviewer@example.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La evidencia no es suficiente"
                );

        assertThat(result)
                .isSameAs(
                        reviewResult
                );

        verify(reviewService)
                .review(
                        stored,
                        "reviewer@example.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La evidencia no es suficiente"
                );
    }

    @Test
    void shouldRejectInvalidStoreIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.review(
                        0L,
                        20L,
                        42L,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "storeId"
                );

        verifyNoInteractions(
                synthesisStore,
                reviewService
        );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.review(
                        10L,
                        null,
                        42L,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "projectId"
                );

        verifyNoInteractions(
                synthesisStore,
                reviewService
        );
    }

    @Test
    void shouldRejectInvalidSynthesisIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.review(
                        10L,
                        20L,
                        -1L,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "synthesisId"
                );

        verifyNoInteractions(
                synthesisStore,
                reviewService
        );
    }

    @Test
    void shouldRejectSnapshotOutsideTenantProject() {
        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        999L
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                command.review(
                        10L,
                        20L,
                        999L,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no pertenece al proyecto"
                );

        verifyNoInteractions(
                reviewService
        );
    }

    @Test
    void shouldRejectNullReviewResult() {
        StoredStrategicSynthesis stored =
                mockStoredSynthesis(
                        42L
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        42L
                )
        ).thenReturn(
                Optional.of(
                        stored
                )
        );

        when(
                reviewService.review(
                        stored,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.review(
                        10L,
                        20L,
                        42L,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "resultado nulo"
                );
    }

    @Test
    void shouldPropagateGovernancePolicyRejection() {
        StoredStrategicSynthesis stored =
                mockStoredSynthesis(
                        42L
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        42L
                )
        ).thenReturn(
                Optional.of(
                        stored
                )
        );

        when(
                reviewService.review(
                        stored,
                        "ai-reviewer",
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Automated approval"
                )
        ).thenThrow(
                new IllegalStateException(
                        "La revisión requiere intervención humana"
                )
        );

        assertThatThrownBy(() ->
                command.review(
                        10L,
                        20L,
                        42L,
                        "ai-reviewer",
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Automated approval"
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "intervención humana"
                );
    }

    @Test
    void shouldPropagateLifecycleRejectionWithoutRetryingReview() {
        StoredStrategicSynthesis stored =
                mockStoredSynthesis(
                        42L
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        42L
                )
        ).thenReturn(
                Optional.of(
                        stored
                )
        );

        when(
                reviewService.review(
                        stored,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                )
        ).thenThrow(
                new IllegalStateException(
                        "Solo una síntesis REQUIRES_REVIEW puede recibir una decisión de revisión"
                )
        );

        assertThatThrownBy(() ->
                command.review(
                        10L,
                        20L,
                        42L,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "REQUIRES_REVIEW"
                );

        verify(reviewService, times(1))
                .review(
                        stored,
                        "jovani",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved"
                );
    }

    @Test
    void shouldUseTenantProjectAndSnapshotIdentity() {
        StoredStrategicSynthesis stored =
                mockStoredSynthesis(
                        900L
                );

        ReviewStrategicSynthesisResult reviewResult =
                mock(
                        ReviewStrategicSynthesisResult.class
                );

        when(
                synthesisStore.findSnapshot(
                        77L,
                        200L,
                        900L
                )
        ).thenReturn(
                Optional.of(
                        stored
                )
        );

        when(
                reviewService.review(
                        stored,
                        "reviewer",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.REJECT,
                        "Requires changes"
                )
        ).thenReturn(
                reviewResult
        );

        command.review(
                77L,
                200L,
                900L,
                "reviewer",
                StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                StrategicSynthesisReviewDecision.REJECT,
                "Requires changes"
        );

        verify(synthesisStore)
                .findSnapshot(
                        77L,
                        200L,
                        900L
                );

        verifyNoMoreInteractions(
                synthesisStore
        );
    }

    private static StoredStrategicSynthesis mockStoredSynthesis(
            Long id
    ) {
        StrategicSynthesis synthesis =
                mock(
                        StrategicSynthesis.class
                );

        return new StoredStrategicSynthesis(
                id,
                synthesis,
                Instant.parse(
                        "2026-08-21T02:00:00Z"
                )
        );
    }
}
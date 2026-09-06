package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewStrategicSynthesisServiceTest {

    @Mock
    private StrategicSynthesisReviewPolicy reviewPolicy;

    @Mock
    private StrategicSynthesisGovernanceProvenanceRecorder provenanceRecorder;

    @Mock
    private StrategicSynthesisTraceabilityRegistrar traceabilityRegistrar;

    @Mock
    private StrategicSynthesisStore synthesisStore;

    @Mock
    private StrategicSynthesisReviewStore reviewStore;

    private Clock clock;

    private ReviewStrategicSynthesisService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        clock =
                Clock.fixed(
                        Instant.parse(
                                "2026-08-10T18:00:00Z"
                        ),
                        ZoneOffset.UTC
                );

        service =
                new ReviewStrategicSynthesisService(
                        reviewPolicy,
                        traceabilityRegistrar,
                        provenanceRecorder,
                        synthesisStore,
                        reviewStore,
                        clock
                );
    }

    @Test
    void shouldApproveAuthorizedReviewAndPersistGovernanceSnapshots() {
        StoredStrategicSynthesis stored =
                storedSynthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisConfidence.HIGH,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        41L
                );

        StrategicSynthesis synthesis =
                stored.synthesis();

        StrategicSynthesisReviewPolicyResult policyResult =
                authorized();

        when(
                reviewPolicy.evaluate(
                        synthesis,
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE
                )
        ).thenReturn(
                policyResult
        );

        when(
                synthesisStore.saveSnapshot(
                        any(StrategicSynthesis.class)
                )
        ).thenAnswer(invocation -> {
            StrategicSynthesis updated =
                    invocation.getArgument(0);

            return new StoredStrategicSynthesis(
                    42L,
                    updated,
                    Instant.parse(
                            "2026-08-10T18:00:01Z"
                    )
            );
        });

        when(
                reviewStore.save(
                        eq(41L),
                        eq(42L),
                        any(StrategicSynthesisReview.class)
                )
        ).thenAnswer(invocation ->
                new StoredStrategicSynthesisReview(
                        100L,
                        41L,
                        42L,
                        invocation.getArgument(2)
                )
        );

        ReviewStrategicSynthesisResult result =
                service.review(
                        stored,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis representa correctamente la estrategia."
                );

        assertThat(result.previousStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(result.resultingStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.APPROVED
                );

        assertThat(result.synthesis().getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.APPROVED
                );

        assertThat(result.review().approved())
                .isTrue();

        assertThat(result.review().getReviewedAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-08-10T18:00:00Z"
                        )
                );

        assertThat(result.policyResult())
                .isSameAs(
                        policyResult
                );

        /*
         * El snapshot original debe permanecer inmutable.
         */
        assertThat(synthesis.getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        verify(synthesisStore)
                .saveSnapshot(
                        argThat(updated ->
                                updated.getStatus()
                                        == StrategicSynthesisStatus.APPROVED
                        )
                );

        verify(reviewStore)
                .save(
                        eq(41L),
                        eq(42L),
                        argThat(review ->
                                review.approved()
                                        && review.getPreviousStatus()
                                        == StrategicSynthesisStatus.REQUIRES_REVIEW
                                        && review.getResultingStatus()
                                        == StrategicSynthesisStatus.APPROVED
                        )
                );
    }

    @Test
    void shouldRejectAuthorizedReviewAndPersistResultingSnapshot() {
        StoredStrategicSynthesis stored =
                storedSynthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisConfidence.HIGH,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        51L
                );

        StrategicSynthesis synthesis =
                stored.synthesis();

        when(
                reviewPolicy.evaluate(
                        synthesis,
                        StrategicSynthesisReviewerType.PROJECT_OWNER,
                        StrategicSynthesisReviewDecision.REJECT
                )
        ).thenReturn(
                authorized()
        );

        when(
                synthesisStore.saveSnapshot(
                        any(StrategicSynthesis.class)
                )
        ).thenAnswer(invocation ->
                new StoredStrategicSynthesis(
                        52L,
                        invocation.getArgument(0),
                        Instant.parse(
                                "2026-08-10T18:00:01Z"
                        )
                )
        );

        when(
                reviewStore.save(
                        eq(51L),
                        eq(52L),
                        any(StrategicSynthesisReview.class)
                )
        ).thenAnswer(invocation ->
                new StoredStrategicSynthesisReview(
                        101L,
                        51L,
                        52L,
                        invocation.getArgument(2)
                )
        );

        ReviewStrategicSynthesisResult result =
                service.review(
                        stored,
                        "owner@example.com",
                        StrategicSynthesisReviewerType.PROJECT_OWNER,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La tesis necesita ajustes."
                );

        assertThat(result.resultingStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REJECTED
                );

        assertThat(result.review().rejected())
                .isTrue();

        assertThat(result.synthesis().getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REJECTED
                );

        verify(synthesisStore)
                .saveSnapshot(
                        argThat(updated ->
                                updated.getStatus()
                                        == StrategicSynthesisStatus.REJECTED
                        )
                );

        verify(reviewStore)
                .save(
                        eq(51L),
                        eq(52L),
                        any(StrategicSynthesisReview.class)
                );
    }

    @Test
    void shouldBlockSystemWhenHumanReviewIsRequired() {
        StoredStrategicSynthesis stored =
                storedSynthesis(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisConfidence.MEDIUM,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        61L
                );

        StrategicSynthesis synthesis =
                stored.synthesis();

        when(
                reviewPolicy.evaluate(
                        synthesis,
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE
                )
        ).thenReturn(
                humanRequired()
        );

        assertThatThrownBy(() ->
                service.review(
                        stored,
                        "system",
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Automatic approval"
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "intervención humana"
                );

        verifyNoInteractions(
                synthesisStore,
                reviewStore,
                traceabilityRegistrar,
                provenanceRecorder
        );
    }

    @Test
    void shouldRejectUnauthorizedReview() {
        StoredStrategicSynthesis stored =
                storedSynthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisConfidence.HIGH,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        71L
                );

        StrategicSynthesis synthesis =
                stored.synthesis();

        when(
                reviewPolicy.evaluate(
                        synthesis,
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE
                )
        ).thenReturn(
                denied()
        );

        assertThatThrownBy(() ->
                service.review(
                        stored,
                        "system",
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Attempt"
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no está autorizada"
                );

        verifyNoInteractions(
                synthesisStore,
                reviewStore,
                traceabilityRegistrar,
                provenanceRecorder
        );
    }

    @Test
    void shouldNotCreateReviewOrSnapshotsWhenPolicyBlocksOperation() {
        StoredStrategicSynthesis stored =
                storedSynthesis(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisConfidence.MEDIUM,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        81L
                );

        when(
                reviewPolicy.evaluate(
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(
                humanRequired()
        );

        assertThatThrownBy(() ->
                service.review(
                        stored,
                        "system",
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Attempt"
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );

        assertThat(
                stored.synthesis().getStatus()
        ).isEqualTo(
                StrategicSynthesisStatus.REQUIRES_REVIEW
        );

        verifyNoInteractions(
                synthesisStore,
                reviewStore,
                traceabilityRegistrar,
                provenanceRecorder
        );
    }

    private static StrategicSynthesisReviewPolicyResult authorized() {
        return StrategicSynthesisReviewPolicyResult.of(
                StrategicSynthesisReviewAuthorization.AUTHORIZED,
                StrategicSynthesisReviewRequirement.NONE,
                List.of(
                        new StrategicSynthesisReviewPolicyReason(
                                StrategicSynthesisReviewPolicyReasonCode.REVIEW_AUTHORIZED,
                                "Authorized"
                        )
                )
        );
    }

    private static StrategicSynthesisReviewPolicyResult humanRequired() {
        return StrategicSynthesisReviewPolicyResult.of(
                StrategicSynthesisReviewAuthorization.HUMAN_REVIEW_REQUIRED,
                StrategicSynthesisReviewRequirement.HUMAN_REVIEW,
                List.of(
                        new StrategicSynthesisReviewPolicyReason(
                                StrategicSynthesisReviewPolicyReasonCode.AI_SYNTHESIS_REQUIRES_HUMAN,
                                "Human required"
                        )
                )
        );
    }

    private static StrategicSynthesisReviewPolicyResult denied() {
        return StrategicSynthesisReviewPolicyResult.of(
                StrategicSynthesisReviewAuthorization.NOT_AUTHORIZED,
                StrategicSynthesisReviewRequirement.NONE,
                List.of(
                        new StrategicSynthesisReviewPolicyReason(
                                StrategicSynthesisReviewPolicyReasonCode.SYNTHESIS_NOT_REVIEWABLE,
                                "Denied"
                        )
                )
        );
    }

    private static StoredStrategicSynthesis storedSynthesis(
            StrategicSynthesisOrigin origin,
            StrategicSynthesisConfidence confidence,
            StrategicSynthesisStatus status,
            Long id
    ) {
        return new StoredStrategicSynthesis(
                id,
                synthesis(
                        origin,
                        confidence,
                        status
                ),
                Instant.parse(
                        "2026-08-10T17:00:00Z"
                )
        );
    }

    private static StrategicSynthesis synthesis(
            StrategicSynthesisOrigin origin,
            StrategicSynthesisConfidence confidence,
            StrategicSynthesisStatus status
    ) {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Problem",
                "Objective",
                "Opportunity",
                "Strategic thesis",
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-001"
                        ),
                        4
                ),
                confidence,
                origin,
                status,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }
}
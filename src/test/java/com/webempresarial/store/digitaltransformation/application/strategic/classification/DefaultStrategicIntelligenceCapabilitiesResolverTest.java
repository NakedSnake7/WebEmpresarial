package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.application.strategic.api.DefaultStrategicIntelligenceCapabilitiesResolver;
import com.webempresarial.store.digitaltransformation.application.strategic.api.StrategicAiAvailability;
import com.webempresarial.store.digitaltransformation.application.strategic.api.StrategicIntelligenceCapabilitiesResponse;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultStrategicIntelligenceCapabilitiesResolverTest {

    @Mock
    private StrategicAiAvailability aiAvailability;

    private DefaultStrategicIntelligenceCapabilitiesResolver
            resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        resolver =
                new DefaultStrategicIntelligenceCapabilitiesResolver(
                        aiAvailability
                );
    }

    @Test
    void shouldAllowDeterministicSynthesisWhenGateIsEligible() {
        StrategicTraversalResult traversal =
                traversal();

        StrategicEvidenceCoverage coverage =
                mock(
                        StrategicEvidenceCoverage.class
                );

        StrategicSynthesisGateResult gateResult =
                mock(
                        StrategicSynthesisGateResult.class
                );

        when(
                gateResult.isEligible()
        ).thenReturn(
                true
        );

        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                false
        );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal,
                        coverage,
                        gateResult,
                        null,
                        null
                );

        assertThat(
                response.canGenerateDeterministicSynthesis()
        ).isTrue();

        assertThat(
                response.canRequestAiInterpretation()
        ).isFalse();

        assertThat(
                response.aiAvailable()
        ).isFalse();
    }

    @Test
    void shouldNotAllowDeterministicSynthesisWithoutCoverage() {
        StrategicTraversalResult traversal =
                traversal();

        StrategicSynthesisGateResult gateResult =
                mock(
                        StrategicSynthesisGateResult.class
                );

        when(
                gateResult.isEligible()
        ).thenReturn(
                true
        );

        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                false
        );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal,
                        null,
                        gateResult,
                        null,
                        null
                );

        assertThat(
                response.canGenerateDeterministicSynthesis()
        ).isFalse();
    }

    @Test
    void shouldAllowAiInterpretationForReadyDeterministicSynthesisWhenAiIsAvailable() {
        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                true
        );

        StoredStrategicSynthesis deterministic =
                stored(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        41L
                );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal(),
                        null,
                        null,
                        deterministic,
                        null
                );

        assertThat(
                response.aiAvailable()
        ).isTrue();

        assertThat(
                response.canRequestAiInterpretation()
        ).isTrue();
    }

    @Test
    void shouldNotAllowAiInterpretationWhenAiIsUnavailable() {
        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                false
        );

        StoredStrategicSynthesis deterministic =
                stored(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        41L
                );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal(),
                        null,
                        null,
                        deterministic,
                        null
                );

        assertThat(
                response.aiAvailable()
        ).isFalse();

        assertThat(
                response.canRequestAiInterpretation()
        ).isFalse();
    }

    @Test
    void shouldNotAllowAiInterpretationForNonDeterministicSynthesis() {
        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                true
        );

        StoredStrategicSynthesis ai =
                stored(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.READY,
                        51L
                );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal(),
                        null,
                        null,
                        ai,
                        null
                );

        assertThat(
                response.canRequestAiInterpretation()
        ).isFalse();
    }

    @Test
    void shouldExposeReviewCapabilitiesForAiSynthesisRequiringReview() {
        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                true
        );

        StoredStrategicSynthesis deterministic =
                stored(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        41L
                );

        StoredStrategicSynthesis ai =
                stored(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        42L
                );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal(),
                        null,
                        null,
                        deterministic,
                        ai
                );

        assertThat(
                response.canSubmitForReview()
        ).isFalse();

        assertThat(
                response.canApprove()
        ).isTrue();

        assertThat(
                response.canReject()
        ).isTrue();
    }

    @Test
    void shouldAllowSubmitForReviewForReadyDeterministicSynthesisWhenNoAiExists() {
        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                false
        );

        StoredStrategicSynthesis deterministic =
                stored(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        41L
                );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal(),
                        null,
                        null,
                        deterministic,
                        null
                );

        assertThat(
                response.canSubmitForReview()
        ).isTrue();

        assertThat(
                response.canApprove()
        ).isFalse();

        assertThat(
                response.canReject()
        ).isFalse();
    }

    @Test
    void shouldNotAllowFurtherGovernanceActionsForApprovedSynthesis() {
        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                true
        );

        StoredStrategicSynthesis approvedAi =
                stored(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.APPROVED,
                        42L
                );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal(),
                        null,
                        null,
                        null,
                        approvedAi
                );

        assertThat(
                response.canSubmitForReview()
        ).isFalse();

        assertThat(
                response.canApprove()
        ).isFalse();

        assertThat(
                response.canReject()
        ).isFalse();
    }

    @Test
    void shouldPreferAiSynthesisAsGovernanceCandidate() {
        when(
                aiAvailability.isAvailable()
        ).thenReturn(
                true
        );

        StoredStrategicSynthesis deterministic =
                stored(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        41L
                );

        StoredStrategicSynthesis ai =
                stored(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        42L
                );

        StrategicIntelligenceCapabilitiesResponse response =
                resolver.resolve(
                        traversal(),
                        null,
                        null,
                        deterministic,
                        ai
                );

        /*
         * Si se utilizara la determinista como candidato,
         * canSubmitForReview sería true.
         *
         * Debe prevalecer AI_ASSISTED.
         */
        assertThat(
                response.canSubmitForReview()
        ).isFalse();

        assertThat(
                response.canApprove()
        ).isTrue();

        assertThat(
                response.canReject()
        ).isTrue();
    }

    @Test
    void shouldRejectNullTraversal() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        null,
                        null,
                        null,
                        null,
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "StrategicTraversalResult"
                );

        verifyNoInteractions(
                aiAvailability
        );
    }

    private static StrategicTraversalResult traversal() {
        return StrategicTraversalResult.of(
                StrategicTraversalStatus.INCOMPLETE,
                null,
                null,
                null,
                null,
                List.of(),
                List.of()
        );
    }

    private static StoredStrategicSynthesis stored(
            StrategicSynthesisOrigin origin,
            StrategicSynthesisStatus status,
            Long id
    ) {
        return new StoredStrategicSynthesis(
                id,
                synthesis(
                        origin,
                        status
                ),
                Instant.parse(
                        "2026-08-14T18:00:00Z"
                )
        );
    }

    private static StrategicSynthesis synthesis(
            StrategicSynthesisOrigin origin,
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
                StrategicSynthesisConfidence.HIGH,
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
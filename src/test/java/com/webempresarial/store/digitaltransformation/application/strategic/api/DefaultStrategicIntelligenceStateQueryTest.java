package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultStrategicIntelligenceStateQueryTest {

    @Mock
    private StrategicGraphTraversalEngine traversalEngine;

    @Mock
    private StrategicChainEvidenceCoverageEvaluator coverageEvaluator;

    @Mock
    private StrategicSynthesisGate synthesisGate;

    @Mock
    private StrategicSynthesisStore synthesisStore;

    @Mock
    private StrategicSynthesisReviewStore reviewStore;

    @Mock
    private StrategicIntelligenceCapabilitiesResolver capabilitiesResolver;

    private DefaultStrategicIntelligenceStateQuery query;
    
    private static final Long DETERMINISTIC_SYNTHESIS_ID = 41L;
    private static final Long AI_SYNTHESIS_ID = 42L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        query =
                new DefaultStrategicIntelligenceStateQuery(
                        traversalEngine,
                        coverageEvaluator,
                        synthesisGate,
                        synthesisStore,
                        reviewStore,
                        capabilitiesResolver
                );
    }
    
    

    @Test
    void shouldExposeAiSynthesisAsReviewableWhenAiRequiresReview() {
        StoredStrategicSynthesis deterministic =
                storedSynthesis(
                        DETERMINISTIC_SYNTHESIS_ID,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY
                );

        StoredStrategicSynthesis ai =
                storedSynthesis(
                        AI_SYNTHESIS_ID,
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        configureState(
                deterministic,
                ai
        );

        StrategicIntelligenceStateResponse result =
                query.findState(
                        10L,
                        20L,
                        30L
                );

        assertThat(
                result.reviewableSynthesisId()
        ).isEqualTo(
                AI_SYNTHESIS_ID
        );
    }
    
    @Test
    void shouldExposeDeterministicSynthesisAsReviewableWhenItRequiresReview() {
        StoredStrategicSynthesis deterministic =
                storedSynthesis(
                        DETERMINISTIC_SYNTHESIS_ID,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        configureState(
                deterministic,
                null
        );

        StrategicIntelligenceStateResponse result =
                query.findState(
                        10L,
                        20L,
                        30L
                );

        assertThat(
                result.reviewableSynthesisId()
        ).isEqualTo(
                DETERMINISTIC_SYNTHESIS_ID
        );
    }
    
    @Test
    void shouldNotExposeReviewableSynthesisWhenNoSnapshotRequiresReview() {
        StoredStrategicSynthesis deterministic =
                storedSynthesis(
                        DETERMINISTIC_SYNTHESIS_ID,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY
                );

        StoredStrategicSynthesis ai =
                storedSynthesis(
                        AI_SYNTHESIS_ID,
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.APPROVED
                );

        configureState(
                deterministic,
                ai
        );

        StrategicIntelligenceStateResponse result =
                query.findState(
                        10L,
                        20L,
                        30L
                );

        assertThat(
                result.reviewableSynthesisId()
        ).isNull();
    }
    @Test
    void shouldPreferAiSynthesisWhenBothSnapshotsRequireReview() {
        StoredStrategicSynthesis deterministic =
                storedSynthesis(
                        DETERMINISTIC_SYNTHESIS_ID,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StoredStrategicSynthesis ai =
                storedSynthesis(
                        AI_SYNTHESIS_ID,
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        configureState(
                deterministic,
                ai
        );

        StrategicIntelligenceStateResponse result =
                query.findState(
                        10L,
                        20L,
                        30L
                );

        assertThat(
                result.reviewableSynthesisId()
        ).isEqualTo(
                AI_SYNTHESIS_ID
        );
    }
    
    @Test
    void shouldComposeCompleteStrategicIntelligenceState() {
        StrategicTraversalResult traversal =
                completeTraversal();

        StrategicChain chain =
                traversal.toChain()
                        .orElseThrow();

        StrategicEvidenceCoverage coverage =
                mock(
                        StrategicEvidenceCoverage.class
                );

        StrategicSynthesisGateResult gateResult =
                mock(
                        StrategicSynthesisGateResult.class
                );

        StoredStrategicSynthesis deterministic =
                storedSynthesis(
                        41L,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY
                );

        StoredStrategicSynthesis ai =
                storedSynthesis(
                        42L,
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StoredStrategicSynthesisReview review =
                storedReview(
                        100L,
                        42L,
                        43L
                );

        StrategicIntelligenceCapabilitiesResponse capabilities =
                new StrategicIntelligenceCapabilitiesResponse(
                        true,
                        true,
                        false,
                        true,
                        true,
                        true
                );

        when(
                traversalEngine.traverseFromFinding(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                traversal
        );

        when(
                coverageEvaluator.evaluate(
                        any(StrategicChain.class)
                )
        ).thenReturn(
                coverage
        );

        when(
                synthesisGate.evaluate(
                        traversal,
                        coverage
                )
        ).thenReturn(
                gateResult
        );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        deterministic
                )
        );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.AI_ASSISTED
                )
        ).thenReturn(
                Optional.of(
                        ai
                )
        );

        when(
                reviewStore.findAllByProject(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        review
                )
        );

        when(
                capabilitiesResolver.resolve(
                        traversal,
                        coverage,
                        gateResult,
                        deterministic,
                        ai
                )
        ).thenReturn(
                capabilities
        );

        StrategicIntelligenceStateResponse response =
                query.findState(
                        10L,
                        20L,
                        30L
                );

        assertThat(response.projectId())
                .isEqualTo(
                        20L
                );

        assertThat(response.findingArtifactId())
                .isEqualTo(
                        30L
                );

        assertThat(response.chain())
                .isNotNull();

        assertThat(response.chain().complete())
                .isTrue();

        assertThat(response.chain().finding().code())
                .isEqualTo(
                        "FND-001"
                );

        assertThat(response.deterministicSynthesis())
                .isNotNull();

        assertThat(response.deterministicSynthesis().id())
                .isEqualTo(
                        41L
                );

        assertThat(response.aiSynthesis())
                .isNotNull();

        assertThat(response.aiSynthesis().id())
                .isEqualTo(
                        42L
                );

        assertThat(response.reviews())
                .hasSize(
                        1
                );

        assertThat(response.reviews().get(0).id())
                .isEqualTo(
                        100L
                );

        assertThat(response.capabilities())
                .isSameAs(
                        capabilities
                );

        verify(coverageEvaluator)
                .evaluate(
                        any(StrategicChain.class)
                );

        verify(synthesisGate)
                .evaluate(
                        traversal,
                        coverage
                );

        verify(capabilitiesResolver)
                .resolve(
                        traversal,
                        coverage,
                        gateResult,
                        deterministic,
                        ai
                );
    }

    @Test
    void shouldReturnStateWithoutCoverageWhenTraversalCannotBuildChain() {
        StrategicTraversalResult traversal =
                StrategicTraversalResult.of(
                        StrategicTraversalStatus.AMBIGUOUS,
                        finding(),
                        null,
                        null,
                        null,
                        List.of(),
                        List.of()
                );

        StrategicIntelligenceCapabilitiesResponse capabilities =
                new StrategicIntelligenceCapabilitiesResponse(
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                );

        when(
                traversalEngine.traverseFromFinding(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                traversal
        );

        when(
                synthesisStore.findLatestSnapshot(
                        anyLong(),
                        anyLong(),
                        any()
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                reviewStore.findAllByProject(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of()
        );

        when(
                capabilitiesResolver.resolve(
                        traversal,
                        null,
                        null,
                        null,
                        null
                )
        ).thenReturn(
                capabilities
        );

        StrategicIntelligenceStateResponse response =
                query.findState(
                        10L,
                        20L,
                        30L
                );

        assertThat(response.chain())
                .isNotNull();

        assertThat(response.chain().ambiguous())
                .isTrue();

        assertThat(response.evidenceCoverage())
                .isNull();

        assertThat(response.deterministicSynthesis())
                .isNull();

        assertThat(response.aiSynthesis())
                .isNull();

        assertThat(response.reviews())
                .isEmpty();

        verifyNoInteractions(
                coverageEvaluator,
                synthesisGate
        );
    }

    @Test
    void shouldEvaluateCoverageForIncompleteTraversalWhenChainCanBeBuilt() {
        StrategicTraversalResult traversal =
                incompleteTraversal();

        StrategicEvidenceCoverage coverage =
                mock(
                        StrategicEvidenceCoverage.class
                );

        StrategicSynthesisGateResult gateResult =
                mock(
                        StrategicSynthesisGateResult.class
                );

        StrategicIntelligenceCapabilitiesResponse capabilities =
                new StrategicIntelligenceCapabilitiesResponse(
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                );

        when(
                traversalEngine.traverseFromFinding(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                traversal
        );

        when(
                coverageEvaluator.evaluate(
                        any(StrategicChain.class)
                )
        ).thenReturn(
                coverage
        );

        when(
                synthesisGate.evaluate(
                        traversal,
                        coverage
                )
        ).thenReturn(
                gateResult
        );

        when(
                synthesisStore.findLatestSnapshot(
                        anyLong(),
                        anyLong(),
                        any()
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                reviewStore.findAllByProject(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of()
        );

        when(
                capabilitiesResolver.resolve(
                        traversal,
                        coverage,
                        gateResult,
                        null,
                        null
                )
        ).thenReturn(
                capabilities
        );

        StrategicIntelligenceStateResponse response =
                query.findState(
                        10L,
                        20L,
                        30L
                );

        assertThat(response.chain().complete())
                .isFalse();

        assertThat(response.chain().canBuildChain())
                .isTrue();

        verify(coverageEvaluator)
                .evaluate(
                        any(StrategicChain.class)
                );

        verify(synthesisGate)
                .evaluate(
                        traversal,
                        coverage
                );
    }

    @Test
    void shouldReturnEmptySynthesisSlotsWhenNoSnapshotsExist() {
        StrategicTraversalResult traversal =
                completeTraversal();

        StrategicEvidenceCoverage coverage =
                mock(
                        StrategicEvidenceCoverage.class
                );

        StrategicSynthesisGateResult gateResult =
                mock(
                        StrategicSynthesisGateResult.class
                );

        StrategicIntelligenceCapabilitiesResponse capabilities =
                new StrategicIntelligenceCapabilitiesResponse(
                        true,
                        false,
                        false,
                        false,
                        false,
                        false
                );

        when(
                traversalEngine.traverseFromFinding(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                traversal
        );

        when(
                coverageEvaluator.evaluate(
                        any(StrategicChain.class)
                )
        ).thenReturn(
                coverage
        );

        when(
                synthesisGate.evaluate(
                        traversal,
                        coverage
                )
        ).thenReturn(
                gateResult
        );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.AI_ASSISTED
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                reviewStore.findAllByProject(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of()
        );

        when(
                capabilitiesResolver.resolve(
                        traversal,
                        coverage,
                        gateResult,
                        null,
                        null
                )
        ).thenReturn(
                capabilities
        );

        StrategicIntelligenceStateResponse response =
                query.findState(
                        10L,
                        20L,
                        30L
                );

        assertThat(
                response.deterministicSynthesis()
        ).isNull();

        assertThat(
                response.aiSynthesis()
        ).isNull();

        assertThat(
                response.reviews()
        ).isEmpty();
    }

    @Test
    void shouldRejectInvalidStoreIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                query.findState(
                        0L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "storeId"
                );

        verifyNoInteractions(
                traversalEngine,
                coverageEvaluator,
                synthesisGate,
                synthesisStore,
                reviewStore,
                capabilitiesResolver
        );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                query.findState(
                        10L,
                        null,
                        30L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "projectId"
                );

        verifyNoInteractions(
                traversalEngine,
                coverageEvaluator,
                synthesisGate,
                synthesisStore,
                reviewStore,
                capabilitiesResolver
        );
    }

    @Test
    void shouldRejectInvalidFindingArtifactIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                query.findState(
                        10L,
                        20L,
                        -1L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "findingArtifactId"
                );

        verifyNoInteractions(
                traversalEngine,
                coverageEvaluator,
                synthesisGate,
                synthesisStore,
                reviewStore,
                capabilitiesResolver
        );
    }

    @Test
    void shouldRejectNullTraversalReturnedByEngine() {
        when(
                traversalEngine.traverseFromFinding(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                query.findState(
                        10L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "resultado nulo"
                );

        verifyNoInteractions(
                coverageEvaluator,
                synthesisGate,
                synthesisStore,
                reviewStore,
                capabilitiesResolver
        );
    }

    @Test
    void shouldRejectNullReviewListReturnedByStore() {
        StrategicTraversalResult traversal =
                completeTraversal();

        StrategicEvidenceCoverage coverage =
                mock(
                        StrategicEvidenceCoverage.class
                );

        StrategicSynthesisGateResult gateResult =
                mock(
                        StrategicSynthesisGateResult.class
                );

        when(
                traversalEngine.traverseFromFinding(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                traversal
        );

        when(
                coverageEvaluator.evaluate(
                        any(StrategicChain.class)
                )
        ).thenReturn(
                coverage
        );

        when(
                synthesisGate.evaluate(
                        traversal,
                        coverage
                )
        ).thenReturn(
                gateResult
        );

        when(
                synthesisStore.findLatestSnapshot(
                        anyLong(),
                        anyLong(),
                        any()
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                reviewStore.findAllByProject(
                        10L,
                        20L
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                query.findState(
                        10L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "lista nula"
                );

        verifyNoInteractions(
                capabilitiesResolver
        );
    }

    private static StrategicTraversalResult completeTraversal() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicTraversalResult.of(
                StrategicTraversalStatus.COMPLETE,
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        "Finding"
                ),
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        "Problem"
                ),
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        "Objective"
                ),
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        "Opportunity"
                ),
                List.of(),
                List.of()
        );
    }

    private static StrategicTraversalResult incompleteTraversal() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicTraversalResult.of(
                StrategicTraversalStatus.INCOMPLETE,
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        "Finding"
                ),
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        "Problem"
                ),
                null,
                null,
                List.of(
                        new StrategicChainGap(
                                StrategicChainGapType.MISSING_BUSINESS_OBJECTIVE,
                                "Missing objective"
                        )
                ),
                List.of()
        );
    }

    private static StrategicArtifact finding() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return artifact(
                evidence,
                "FND-001",
                StrategicArtifactType.FINDING,
                "Finding"
        );
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            String code,
            StrategicArtifactType type,
            String statement
    ) {
        return StrategicArtifact.create(
                evidence.getProject(),
                code,
                type,
                StrategicConfidence.STRONGLY_SUPPORTED,
                StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                statement,
                "Rationale",
                "Business implication"
        );
    }

    private static StoredStrategicSynthesis storedSynthesis(
            Long id,
            StrategicSynthesisOrigin origin,
            StrategicSynthesisStatus status
    ) {
        return new StoredStrategicSynthesis(
                id,
                synthesis(
                        origin,
                        status
                ),
                Instant.parse(
                        "2026-08-17T18:00:00Z"
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

    private static StoredStrategicSynthesisReview storedReview(
            Long reviewId,
            Long reviewedSynthesisId,
            Long resultingSynthesisId
    ) {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        reviewed,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved.",
                        Instant.parse(
                                "2026-08-17T18:10:00Z"
                        )
                );

        return new StoredStrategicSynthesisReview(
                reviewId,
                reviewedSynthesisId,
                resultingSynthesisId,
                review
        );
    }
    
    private void configureState(
            StoredStrategicSynthesis deterministic,
            StoredStrategicSynthesis ai
    ) {
        StrategicTraversalResult traversal =
                completeTraversal();

        StrategicEvidenceCoverage coverage =
                mock(
                        StrategicEvidenceCoverage.class
                );

        StrategicSynthesisGateResult gateResult =
                mock(
                        StrategicSynthesisGateResult.class
                );

        StrategicIntelligenceCapabilitiesResponse capabilities =
                mock(
                        StrategicIntelligenceCapabilitiesResponse.class
                );

        when(
                traversalEngine.traverseFromFinding(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                traversal
        );

        when(
                coverageEvaluator.evaluate(
                        any(StrategicChain.class)
                )
        ).thenReturn(
                coverage
        );

        when(
                synthesisGate.evaluate(
                        traversal,
                        coverage
                )
        ).thenReturn(
                gateResult
        );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.ofNullable(
                        deterministic
                )
        );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.AI_ASSISTED
                )
        ).thenReturn(
                Optional.ofNullable(
                        ai
                )
        );

        when(
                reviewStore.findAllByProject(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of()
        );

        when(
                capabilitiesResolver.resolve(
                        eq(traversal),
                        eq(coverage),
                        eq(gateResult),
                        eq(deterministic),
                        eq(ai)
                )
        ).thenReturn(
                capabilities
        );
    }
    
    
   
}
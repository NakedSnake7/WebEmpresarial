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

class DefaultGenerateStrategicSynthesisCommandTest {

    @Mock
    private StrategicGraphTraversalEngine traversalEngine;

    @Mock
    private StrategicChainEvidenceCoverageEvaluator coverageEvaluator;

    @Mock
    private StrategicSynthesisGate synthesisGate;

    @Mock
    private StrategicSynthesisBuilder synthesisBuilder;

    @Mock
    private StrategicSynthesisStore synthesisStore;

    private DefaultGenerateStrategicSynthesisCommand command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        command =
                new DefaultGenerateStrategicSynthesisCommand(
                        traversalEngine,
                        coverageEvaluator,
                        synthesisGate,
                        synthesisBuilder,
                        synthesisStore
                );
    }

    @Test
    void shouldGenerateAndPersistDeterministicSynthesis() {
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

        StrategicSynthesis synthesis =
                synthesis();

        StoredStrategicSynthesis stored =
                new StoredStrategicSynthesis(
                        41L,
                        synthesis,
                        Instant.parse(
                                "2026-08-20T18:00:00Z"
                        )
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
                        eq(traversal),
                        eq(coverage)
                )
        ).thenReturn(
                gateResult
        );

        when(
                gateResult.isEligible()
        ).thenReturn(
                true
        );

        when(
                synthesisBuilder.build(
                        any(StrategicChain.class),
                        eq(coverage),
                        eq(gateResult)
                )
        ).thenReturn(
                synthesis
        );

        when(
                synthesisStore.saveSnapshot(
                        synthesis
                )
        ).thenReturn(
                stored
        );

        GenerateStrategicSynthesisResult result =
                command.generate(
                        10L,
                        20L,
                        30L
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.synthesis())
                .isSameAs(
                        stored
                );

        assertThat(result.synthesis().id())
                .isEqualTo(
                        41L
                );

        assertThat(result.gateResult())
                .isSameAs(
                        gateResult
                );

        verify(traversalEngine)
                .traverseFromFinding(
                        10L,
                        20L,
                        30L
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

        verify(synthesisBuilder)
                .build(
                        any(StrategicChain.class),
                        eq(coverage),
                        eq(gateResult)
                );

        verify(synthesisStore)
                .saveSnapshot(
                        synthesis
                );
    }

    @Test
    void shouldRejectInvalidStoreIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.generate(
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
                synthesisBuilder,
                synthesisStore
        );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.generate(
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
                synthesisBuilder,
                synthesisStore
        );
    }

    @Test
    void shouldRejectInvalidFindingArtifactIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.generate(
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
                synthesisBuilder,
                synthesisStore
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
                command.generate(
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
                synthesisBuilder,
                synthesisStore
        );
    }

    @Test
    void shouldRejectTraversalThatCannotBuildStrategicChain() {
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

        when(
                traversalEngine.traverseFromFinding(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                traversal
        );

        assertThatThrownBy(() ->
                command.generate(
                        10L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "cadena estratégica"
                );

        verifyNoInteractions(
                coverageEvaluator,
                synthesisGate,
                synthesisBuilder,
                synthesisStore
        );
    }

    @Test
    void shouldRejectNullCoverageWithoutCallingGateOrPersistence() {
        StrategicTraversalResult traversal =
                completeTraversal();

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
                null
        );

        assertThatThrownBy(() ->
                command.generate(
                        10L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "CoverageEvaluator"
                );

        verifyNoInteractions(
                synthesisGate,
                synthesisBuilder,
                synthesisStore
        );
    }

    @Test
    void shouldRejectNullGateResultWithoutBuildingOrPersisting() {
        StrategicTraversalResult traversal =
                completeTraversal();

        StrategicEvidenceCoverage coverage =
                mock(
                        StrategicEvidenceCoverage.class
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
                        eq(traversal),
                        eq(coverage)
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.generate(
                        10L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "SynthesisGate"
                );

        verifyNoInteractions(
                synthesisBuilder,
                synthesisStore
        );
    }

    @Test
    void shouldNotBuildOrPersistWhenSynthesisGateRejectsChain() {
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
                        eq(traversal),
                        eq(coverage)
                )
        ).thenReturn(
                gateResult
        );

        when(
                gateResult.isEligible()
        ).thenReturn(
                false
        );

        assertThatThrownBy(() ->
                command.generate(
                        10L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no está autorizada"
                );

        verifyNoInteractions(
                synthesisBuilder,
                synthesisStore
        );
    }

  
    @Test
    void shouldReuseEquivalentReadyDeterministicSnapshot() {

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

        /*
         * Dos instancias distintas,
         * materialmente equivalentes.
         */
        StrategicSynthesis existingSynthesis =
                synthesis();

        StrategicSynthesis generated =
                synthesis();

        assertThat(existingSynthesis)
                .isNotSameAs(generated);

        StoredStrategicSynthesis existing =
                new StoredStrategicSynthesis(
                        41L,
                        existingSynthesis,
                        Instant.parse(
                                "2026-08-21T18:00:00Z"
                        )
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
                gateResult.isEligible()
        ).thenReturn(
                true
        );

        when(
                synthesisBuilder.build(
                        any(StrategicChain.class),
                        eq(coverage),
                        eq(gateResult)
                )
        ).thenReturn(
                generated
        );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        existing
                )
        );

        GenerateStrategicSynthesisResult result =
                command.generate(
                        10L,
                        20L,
                        30L
                );

        assertThat(result.synthesis())
                .isSameAs(
                        existing
                );

        verify(
                synthesisStore,
                never()
        ).saveSnapshot(
                any()
        );
    }    
    @Test
    void shouldRejectNullSynthesisReturnedByBuilderWithoutPersisting() {
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
                gateResult.isEligible()
        ).thenReturn(
                true
        );

        when(
                synthesisBuilder.build(
                        any(StrategicChain.class),
                        eq(coverage),
                        eq(gateResult)
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.generate(
                        10L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "síntesis nula"
                );

        verifyNoInteractions(
                synthesisStore
        );
    }

    @Test
    void shouldCreateNewSnapshotWhenLatestDeterministicWasRejected() {

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

        StrategicSynthesis previous =
                synthesis().withStatus(
                        StrategicSynthesisStatus.REJECTED
                );

        StrategicSynthesis generated =
                synthesis();

        StoredStrategicSynthesis previousStored =
                new StoredStrategicSynthesis(
                        41L,
                        previous,
                        Instant.parse(
                                "2026-08-21T18:00:00Z"
                        )
                );

        StoredStrategicSynthesis newStored =
                new StoredStrategicSynthesis(
                        42L,
                        generated,
                        Instant.parse(
                                "2026-08-21T18:05:00Z"
                        )
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
                        eq(traversal),
                        eq(coverage)
                )
        ).thenReturn(
                gateResult
        );

        when(
                gateResult.isEligible()
        ).thenReturn(
                true
        );

        when(
                synthesisBuilder.build(
                        any(StrategicChain.class),
                        eq(coverage),
                        eq(gateResult)
                )
        ).thenReturn(
                generated
        );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        previousStored
                )
        );

        when(
                synthesisStore.saveSnapshot(
                        generated
                )
        ).thenReturn(
                newStored
        );

        GenerateStrategicSynthesisResult result =
                command.generate(
                        10L,
                        20L,
                        30L
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.synthesis())
                .isSameAs(
                        newStored
                );

        assertThat(result.synthesis().id())
                .isEqualTo(
                        42L
                );

        assertThat(result.gateResult())
                .isSameAs(
                        gateResult
                );

        verify(
                synthesisStore
        ).saveSnapshot(
                generated
        );
    }
    
    @Test
    void shouldRejectNullStoredSnapshotReturnedByStore() {
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

        StrategicSynthesis synthesis =
                synthesis();

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
                        eq(traversal),
                        eq(coverage)
                )
        ).thenReturn(
                gateResult
        );

        when(
                gateResult.isEligible()
        ).thenReturn(
                true
        );

        when(
                synthesisBuilder.build(
                        any(StrategicChain.class),
                        eq(coverage),
                        eq(gateResult)
                )
        ).thenReturn(
                synthesis
        );

        when(
                synthesisStore.saveSnapshot(
                        synthesis
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.generate(
                        10L,
                        20L,
                        30L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "snapshot nulo"
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
                        "Business problem"
                ),
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        "Business objective"
                ),
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        "Strategic opportunity"
                ),
                List.of(),
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

    private static StrategicSynthesis synthesis() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Business problem",
                "Business objective",
                "Strategic opportunity",
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
                StrategicSynthesisOrigin.DETERMINISTIC,
                StrategicSynthesisStatus.READY,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }
}
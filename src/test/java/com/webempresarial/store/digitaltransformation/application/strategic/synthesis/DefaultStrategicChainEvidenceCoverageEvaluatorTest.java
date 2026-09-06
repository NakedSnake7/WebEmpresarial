package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultStrategicChainEvidenceCoverageEvaluatorTest {

    @Mock
    private StrategicEvidenceLineageAnalyzer lineageAnalyzer;

    private DefaultStrategicChainEvidenceCoverageEvaluator evaluator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        evaluator =
                new DefaultStrategicChainEvidenceCoverageEvaluator(
                        lineageAnalyzer
                );
    }

    @Test
    void shouldEvaluateCompleteFullySupportedChain() {
        TestChain context =
                completeChain();

        when(lineageAnalyzer.analyze(context.finding()))
                .thenReturn(direct(context.finding()));

        when(lineageAnalyzer.analyze(context.problem()))
                .thenReturn(inherited(context.problem(), 2));

        when(lineageAnalyzer.analyze(context.objective()))
                .thenReturn(inherited(context.objective(), 3));

        when(lineageAnalyzer.analyze(context.opportunity()))
                .thenReturn(inherited(context.opportunity(), 4));

        StrategicEvidenceCoverage coverage =
                evaluator.evaluate(
                        context.chain()
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED
                );

        assertThat(coverage.coveragePercentage())
                .isEqualTo(100);

        assertThat(coverage.getSupportedArtifacts())
                .isEqualTo(4);

        assertThat(coverage.canProceedToSynthesis())
                .isTrue();

        verify(lineageAnalyzer)
                .analyze(context.finding());

        verify(lineageAnalyzer)
                .analyze(context.problem());

        verify(lineageAnalyzer)
                .analyze(context.objective());

        verify(lineageAnalyzer)
                .analyze(context.opportunity());

        verifyNoMoreInteractions(lineageAnalyzer);
    }

    @Test
    void shouldEvaluateWeakChainAndBlockSynthesis() {
        TestChain context =
                completeChain();

        when(lineageAnalyzer.analyze(context.finding()))
                .thenReturn(weak(context.finding(), 1));

        when(lineageAnalyzer.analyze(context.problem()))
                .thenReturn(weak(context.problem(), 2));

        when(lineageAnalyzer.analyze(context.objective()))
                .thenReturn(weak(context.objective(), 3));

        when(lineageAnalyzer.analyze(context.opportunity()))
                .thenReturn(weak(context.opportunity(), 4));

        StrategicEvidenceCoverage coverage =
                evaluator.evaluate(
                        context.chain()
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.WEAKLY_SUPPORTED
                );

        assertThat(coverage.coveragePercentage())
                .isEqualTo(100);

        assertThat(coverage.canProceedToSynthesis())
                .isFalse();
    }

    @Test
    void shouldEvaluateIncompleteChainWithoutAnalyzingMissingArtifacts() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        problem,
                        null,
                        null
                );

        when(lineageAnalyzer.analyze(finding))
                .thenReturn(direct(finding));

        when(lineageAnalyzer.analyze(problem))
                .thenReturn(inherited(problem, 2));

        StrategicEvidenceCoverage coverage =
                evaluator.evaluate(chain);

        assertThat(coverage.getSupports())
                .hasSize(2);

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED
                );

        /*
         * La evidencia de los artefactos existentes es sólida,
         * pero la cadena estructural sigue incompleta.
         */
        assertThat(coverage.canProceedToSynthesis())
                .isFalse();

        verify(lineageAnalyzer).analyze(finding);
        verify(lineageAnalyzer).analyze(problem);

        verifyNoMoreInteractions(lineageAnalyzer);
    }

    @Test
    void shouldPropagateUnsupportedArtifactsIntoCoverage() {
        TestChain context =
                completeChain();

        when(lineageAnalyzer.analyze(context.finding()))
                .thenReturn(direct(context.finding()));

        when(lineageAnalyzer.analyze(context.problem()))
                .thenReturn(inherited(context.problem(), 2));

        when(lineageAnalyzer.analyze(context.objective()))
                .thenReturn(none(context.objective()));

        when(lineageAnalyzer.analyze(context.opportunity()))
                .thenReturn(none(context.opportunity()));

        StrategicEvidenceCoverage coverage =
                evaluator.evaluate(
                        context.chain()
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.PARTIALLY_SUPPORTED
                );

        assertThat(coverage.coveragePercentage())
                .isEqualTo(50);

        assertThat(coverage.getUnsupportedArtifacts())
                .isEqualTo(2);

        assertThat(coverage.canProceedToSynthesis())
        .isFalse();
    }

    @Test
    void shouldRejectNullChain() {
        assertThatThrownBy(() ->
                evaluator.evaluate(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "cadena estratégica"
                );
    }

    @Test
    void shouldRejectSupportForDifferentArtifact() {
        TestChain context =
                completeChain();

        when(lineageAnalyzer.analyze(context.finding()))
                .thenReturn(
                        direct(context.problem())
                );

        assertThatThrownBy(() ->
                evaluator.evaluate(
                        context.chain()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no corresponde"
                );
    }

    private static StrategicArtifactEvidenceSupport direct(
            StrategicArtifact artifact
    ) {
        return StrategicArtifactEvidenceSupport.direct(
                artifact,
                List.of("EVD-AUDIT-001"),
                TraceabilityStrength.DIRECT,
                "Direct evidence"
        );
    }

    private static StrategicArtifactEvidenceSupport inherited(
            StrategicArtifact artifact,
            int depth
    ) {
        return StrategicArtifactEvidenceSupport.inherited(
                artifact,
                List.of("EVD-AUDIT-001"),
                TraceabilityStrength.STRONG,
                depth,
                "Inherited evidence"
        );
    }

    private static StrategicArtifactEvidenceSupport weak(
            StrategicArtifact artifact,
            int depth
    ) {
        return StrategicArtifactEvidenceSupport.weak(
                artifact,
                List.of("EVD-AUDIT-001"),
                TraceabilityStrength.WEAK,
                depth,
                "Weak evidence"
        );
    }

    private static StrategicArtifactEvidenceSupport none(
            StrategicArtifact artifact
    ) {
        return StrategicArtifactEvidenceSupport.none(
                artifact,
                "No evidence"
        );
    }

    private static TestChain completeChain() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        return new TestChain(
                evidence,
                finding,
                problem,
                objective,
                opportunity,
                chain
        );
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            String code,
            StrategicArtifactType type
    ) {
        return StrategicArtifact.deriveFromEvidence(
                evidence.getProject(),
                evidence,
                code,
                type,
                StrategicConfidence.EXPLICIT,
                StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                "Statement " + code,
                null,
                null
        );
    }

    private record TestChain(
            SourceEvidence evidence,
            StrategicArtifact finding,
            StrategicArtifact problem,
            StrategicArtifact objective,
            StrategicArtifact opportunity,
            StrategicChain chain
    ) {
    }
}
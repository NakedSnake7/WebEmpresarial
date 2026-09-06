package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicEvidenceCoverageTest {

    @Test
    void shouldRecognizeFullySupportedChain() {
        TestChain context =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                direct(context.finding()),
                                inherited(
                                        context.problem(),
                                        2
                                ),
                                inherited(
                                        context.objective(),
                                        3
                                ),
                                inherited(
                                        context.opportunity(),
                                        4
                                )
                        )
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED
                );

        assertThat(coverage.coveragePercentage())
                .isEqualTo(100);

        assertThat(coverage.getSupportedArtifacts())
                .isEqualTo(4);

        assertThat(coverage.getDirectArtifacts())
                .isEqualTo(1);

        assertThat(coverage.getUnsupportedArtifacts())
                .isZero();

        assertThat(coverage.isFullySupported())
                .isTrue();

        assertThat(coverage.canProceedToSynthesis())
                .isTrue();
    }

    @Test
    void shouldRecognizeMostlySupportedChainWithWeakArtifact() {
        TestChain context =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                direct(context.finding()),
                                inherited(
                                        context.problem(),
                                        2
                                ),
                                inherited(
                                        context.objective(),
                                        3
                                ),
                                weak(
                                        context.opportunity(),
                                        4
                                )
                        )
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.MOSTLY_SUPPORTED
                );

        assertThat(coverage.coveragePercentage())
                .isEqualTo(100);

        assertThat(coverage.getWeakArtifacts())
                .isEqualTo(1);
    }

    @Test
    void shouldRecognizePartialSupport() {
        TestChain context =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                direct(context.finding()),
                                inherited(
                                        context.problem(),
                                        2
                                ),
                                none(context.objective()),
                                none(context.opportunity())
                        )
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.PARTIALLY_SUPPORTED
                );

        assertThat(coverage.coveragePercentage())
                .isEqualTo(50);
    }

    @Test
    void shouldRecognizeUnsupportedChain() {
        TestChain context =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                none(context.finding()),
                                none(context.problem()),
                                none(context.objective()),
                                none(context.opportunity())
                        )
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.UNSUPPORTED
                );

        assertThat(coverage.coveragePercentage())
                .isZero();

        assertThat(coverage.canProceedToSynthesis())
                .isFalse();
    }
    @Test
    void shouldRecognizeWeaklySupportedChain() {
        TestChain context =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                weak(context.finding(), 1),
                                weak(context.problem(), 2),
                                weak(context.objective(), 3),
                                weak(context.opportunity(), 4)
                        )
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
    void shouldRejectSupportForArtifactOutsideChain() {
        TestChain context =
                completeChain();

        StrategicArtifact unrelated =
                artifact(
                        context.evidence(),
                        "FND-999",
                        StrategicArtifactType.FINDING
                );

        assertThatThrownBy(() ->
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                direct(context.finding()),
                                inherited(
                                        context.problem(),
                                        2
                                ),
                                inherited(
                                        context.objective(),
                                        3
                                ),
                                direct(unrelated)
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no pertenece"
                );
    }

    private static StrategicArtifactEvidenceSupport direct(
            StrategicArtifact artifact
    ) {
        return StrategicArtifactEvidenceSupport.direct(
                artifact,
                List.of("EVD-AUDIT-001"),
                TraceabilityStrength.DIRECT,
                "Direct support"
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
                "Inherited support"
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
                "Weak support"
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
    
    @Test
    void partiallySupportedChainShouldRequireReviewBeforeSynthesis() {
        TestChain context =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                direct(context.finding()),
                                inherited(context.problem(), 2),
                                none(context.objective()),
                                none(context.opportunity())
                        )
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.PARTIALLY_SUPPORTED
                );

        assertThat(coverage.canProceedToSynthesis())
                .isFalse();
    }
    
    
}
package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactOrigin;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.EvidenceCoverageLevel;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicArtifactEvidenceSupport;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChain;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChainCompleteness;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicEvidenceCoverage;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicEvidenceCoverageStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisConfidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisDecision;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisEvidenceSummary;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisGateReason;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisGateReasonCode;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisGateResult;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisGateSeverity;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisOrigin;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalStatus;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DeterministicStrategicSynthesisBuilderTest {

    @Mock
    private StrategicThesisGenerator thesisGenerator;

    @Mock
    private StrategicSynthesisEvidenceSummaryFactory evidenceSummaryFactory;

    @Mock
    private StrategicSynthesisConfidenceEvaluator confidenceEvaluator;

    private DeterministicStrategicSynthesisBuilder builder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        builder =
                new DeterministicStrategicSynthesisBuilder(
                        thesisGenerator,
                        evidenceSummaryFactory,
                        confidenceEvaluator
                );
    }

    @Test
    void shouldBuildReadyDeterministicSynthesis() {
        TestContext context =
                context();

        StrategicSynthesisEvidenceSummary summary =
                evidenceSummary();

        when(
                evidenceSummaryFactory.create(
                        context.coverage()
                )
        ).thenReturn(
                summary
        );

        when(
                confidenceEvaluator.evaluate(
                        context.chain(),
                        context.coverage()
                )
        ).thenReturn(
                StrategicSynthesisConfidence.HIGH
        );

        when(
                thesisGenerator.generate(
                        context.chain()
                )
        ).thenReturn(
                "La transformación debe resolver el problema actual " +
                "mediante el objetivo y la oportunidad estratégica."
        );

        StrategicSynthesis synthesis =
                builder.build(
                        context.chain(),
                        context.coverage(),
                        context.approvedGate()
                );

        assertThat(synthesis)
                .isNotNull();

        assertThat(synthesis.getOrigin())
                .isEqualTo(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        assertThat(synthesis.getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.READY
                );

        assertThat(synthesis.getConfidence())
                .isEqualTo(
                        StrategicSynthesisConfidence.HIGH
                );

        assertThat(synthesis.getFindingStatement())
                .isEqualTo(
                        context.finding().getStatement()
                );

        assertThat(synthesis.getBusinessProblemStatement())
                .isEqualTo(
                        context.problem().getStatement()
                );

        assertThat(synthesis.getBusinessObjectiveStatement())
                .isEqualTo(
                        context.objective().getStatement()
                );

        assertThat(synthesis.getStrategicOpportunityStatement())
                .isEqualTo(
                        context.opportunity().getStatement()
                );

        assertThat(synthesis.getStrategicThesis())
                .isEqualTo(
                        "La transformación debe resolver el problema actual " +
                        "mediante el objetivo y la oportunidad estratégica."
                );

        assertThat(synthesis.getEvidenceSummary())
                .isSameAs(summary);

        assertThat(synthesis.getSourceArtifactCodes())
                .containsExactly(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                );

        verify(evidenceSummaryFactory)
                .create(
                        context.coverage()
                );

        verify(confidenceEvaluator)
                .evaluate(
                        context.chain(),
                        context.coverage()
                );

        verify(thesisGenerator)
                .generate(
                        context.chain()
                );

        verifyNoMoreInteractions(
                evidenceSummaryFactory,
                confidenceEvaluator,
                thesisGenerator
        );
    }

    @Test
    void shouldPreserveAllApprovedStatementsExactly() {
        TestContext context =
                context();

        when(
                evidenceSummaryFactory.create(
                        context.coverage()
                )
        ).thenReturn(
                evidenceSummary()
        );

        when(
                confidenceEvaluator.evaluate(
                        context.chain(),
                        context.coverage()
                )
        ).thenReturn(
                StrategicSynthesisConfidence.HIGH
        );

        when(
                thesisGenerator.generate(
                        context.chain()
                )
        ).thenReturn(
                "Deterministic thesis"
        );

        StrategicSynthesis synthesis =
                builder.build(
                        context.chain(),
                        context.coverage(),
                        context.approvedGate()
                );

        assertThat(synthesis.getFindingStatement())
                .isEqualTo(
                        "La representación digital no refleja plenamente el valor de la marca."
                );

        assertThat(synthesis.getBusinessProblemStatement())
                .isEqualTo(
                        "La experiencia actual reduce la percepción de autoridad."
                );

        assertThat(synthesis.getBusinessObjectiveStatement())
                .isEqualTo(
                        "Elevar la percepción digital hasta representar el posicionamiento real."
                );

        assertThat(synthesis.getStrategicOpportunityStatement())
                .isEqualTo(
                        "Construir una experiencia digital editorial premium."
                );
    }

    @Test
    void shouldRejectHumanReviewGate() {
        TestContext context =
                context();

        StrategicSynthesisGateResult reviewGate =
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.HUMAN_REVIEW_REQUIRED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.COMPLETE,
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        3,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.ARTIFACT_REQUIRES_REVIEW,
                                        StrategicSynthesisGateSeverity.BLOCKING,
                                        "Human review required"
                                )
                        )
                );

        assertThatThrownBy(() ->
                builder.build(
                        context.chain(),
                        context.coverage(),
                        reviewGate
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no está autorizada"
                );

        verifyNoInteractions(
                thesisGenerator,
                evidenceSummaryFactory,
                confidenceEvaluator
        );
    }

    @Test
    void shouldRejectRejectedGate() {
        TestContext context =
                context();

        StrategicEvidenceCoverage unsupportedCoverage =
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                none(context.finding()),
                                none(context.problem()),
                                none(context.objective()),
                                none(context.opportunity())
                        )
                );

        assertThat(unsupportedCoverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.UNSUPPORTED
                );

        StrategicSynthesisGateResult rejectedGate =
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.REJECTED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.COMPLETE,
                        StrategicEvidenceCoverageStatus.UNSUPPORTED,
                        4,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.EVIDENCE_UNSUPPORTED,
                                        StrategicSynthesisGateSeverity.BLOCKING,
                                        "Unsupported"
                                )
                        )
                );

        assertThatThrownBy(() ->
                builder.build(
                        context.chain(),
                        unsupportedCoverage,
                        rejectedGate
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no está autorizada"
                );

        verifyNoInteractions(
                thesisGenerator,
                evidenceSummaryFactory,
                confidenceEvaluator
        );
    }

    @Test
    void shouldRejectCoverageFromDifferentChain() {
        TestContext context =
                context();

        TestContext different =
                differentContext();

        assertThatThrownBy(() ->
                builder.build(
                        context.chain(),
                        different.coverage(),
                        context.approvedGate()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no corresponden a la misma cadena"
                );

        verifyNoInteractions(
                thesisGenerator,
                evidenceSummaryFactory,
                confidenceEvaluator
        );
    }

    @Test
    void shouldRejectGateWithDifferentEvidenceCoverageStatus() {
        TestContext context =
                context();

        StrategicSynthesisGateResult inconsistentGate =
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.AUTO_APPROVED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.COMPLETE,
                        StrategicEvidenceCoverageStatus.MOSTLY_SUPPORTED,
                        4,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.AUTO_SYNTHESIS_ALLOWED,
                                        StrategicSynthesisGateSeverity.INFO,
                                        "Allowed"
                                )
                        )
                );

        assertThatThrownBy(() ->
                builder.build(
                        context.chain(),
                        context.coverage(),
                        inconsistentGate
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "cobertura de evidencia"
                );

        verifyNoInteractions(
                thesisGenerator,
                evidenceSummaryFactory,
                confidenceEvaluator
        );
    }

    @Test
    void shouldRejectGateWithDifferentCompleteness() {
        TestContext context =
                context();

        StrategicSynthesisGateResult inconsistentGate =
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.AUTO_APPROVED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.THROUGH_OBJECTIVE,
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        4,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.AUTO_SYNTHESIS_ALLOWED,
                                        StrategicSynthesisGateSeverity.INFO,
                                        "Allowed"
                                )
                        )
                );

        assertThatThrownBy(() ->
                builder.build(
                        context.chain(),
                        context.coverage(),
                        inconsistentGate
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "completitud"
                );

        verifyNoInteractions(
                thesisGenerator,
                evidenceSummaryFactory,
                confidenceEvaluator
        );
    }

    @Test
    void shouldRejectNullChain() {
        TestContext context =
                context();

        assertThatThrownBy(() ->
                builder.build(
                        null,
                        context.coverage(),
                        context.approvedGate()
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "cadena estratégica"
                );
    }

    @Test
    void shouldRejectNullCoverage() {
        TestContext context =
                context();

        assertThatThrownBy(() ->
                builder.build(
                        context.chain(),
                        null,
                        context.approvedGate()
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "cobertura de evidencia"
                );
    }

    @Test
    void shouldRejectNullGateResult() {
        TestContext context =
                context();

        assertThatThrownBy(() ->
                builder.build(
                        context.chain(),
                        context.coverage(),
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "synthesis gate"
                );
    }

    private static TestContext context() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        "La representación digital no refleja plenamente el valor de la marca."
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        "La experiencia actual reduce la percepción de autoridad."
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        "Elevar la percepción digital hasta representar el posicionamiento real."
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        "Construir una experiencia digital editorial premium."
                );

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        StrategicEvidenceCoverage coverage =
                fullySupportedCoverage(
                        chain,
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        StrategicSynthesisGateResult gate =
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.AUTO_APPROVED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.COMPLETE,
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        4,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.AUTO_SYNTHESIS_ALLOWED,
                                        StrategicSynthesisGateSeverity.INFO,
                                        "Automatic synthesis allowed"
                                )
                        )
                );

        return new TestContext(
                finding,
                problem,
                objective,
                opportunity,
                chain,
                coverage,
                gate
        );
    }

    private static TestContext differentContext() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-101",
                        StrategicArtifactType.FINDING,
                        "Different finding"
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-101",
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        "Different problem"
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-101",
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        "Different objective"
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-101",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        "Different opportunity"
                );

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        StrategicEvidenceCoverage coverage =
                fullySupportedCoverage(
                        chain,
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        StrategicSynthesisGateResult gate =
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.AUTO_APPROVED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.COMPLETE,
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        4,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.AUTO_SYNTHESIS_ALLOWED,
                                        StrategicSynthesisGateSeverity.INFO,
                                        "Allowed"
                                )
                        )
                );

        return new TestContext(
                finding,
                problem,
                objective,
                opportunity,
                chain,
                coverage,
                gate
        );
    }

    private static StrategicEvidenceCoverage fullySupportedCoverage(
            StrategicChain chain,
            StrategicArtifact finding,
            StrategicArtifact problem,
            StrategicArtifact objective,
            StrategicArtifact opportunity
    ) {
        return StrategicEvidenceCoverage.of(
                chain,
                List.of(
                        direct(finding),
                        inherited(problem, 2),
                        inherited(objective, 3),
                        inherited(opportunity, 4)
                )
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
    
    private static StrategicArtifactEvidenceSupport none(
            StrategicArtifact artifact
    ) {
        return StrategicArtifactEvidenceSupport.none(
                artifact,
                "No evidence"
        );
    }

    private static StrategicSynthesisEvidenceSummary evidenceSummary() {
        return StrategicSynthesisEvidenceSummary.of(
                StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                100,
                List.of("EVD-AUDIT-001"),
                4
        );
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            String code,
            StrategicArtifactType type,
            String statement
    ) {
        return StrategicArtifact.deriveFromEvidence(
                evidence.getProject(),
                evidence,
                code,
                type,
                StrategicConfidence.EXPLICIT,
                StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                statement,
                null,
                null
        );
    }

    private record TestContext(
            StrategicArtifact finding,
            StrategicArtifact problem,
            StrategicArtifact objective,
            StrategicArtifact opportunity,
            StrategicChain chain,
            StrategicEvidenceCoverage coverage,
            StrategicSynthesisGateResult approvedGate
    ) {
    }
}
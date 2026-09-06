package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicSynthesisConfidenceEvaluatorTest {

    private final DefaultStrategicSynthesisConfidenceEvaluator evaluator =
            new DefaultStrategicSynthesisConfidenceEvaluator();

    @Test
    void fullySupportedCompleteChainShouldHaveHighConfidence() {
        Context context =
                context();

        StrategicEvidenceCoverage coverage =
                coverage(
                        context,
                        false
                );

        assertThat(
                evaluator.evaluate(
                        context.chain(),
                        coverage
                )
        ).isEqualTo(
                StrategicSynthesisConfidence.HIGH
        );
    }

    @Test
    void mostlySupportedCompleteChainShouldHaveMediumConfidence() {
        Context context =
                context();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        context.chain(),
                        List.of(
                                direct(context.finding()),
                                inherited(context.problem(), 2),
                                inherited(context.objective(), 3),
                                weak(context.opportunity(), 4)
                        )
                );

        assertThat(coverage.getStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.MOSTLY_SUPPORTED
                );

        assertThat(
                evaluator.evaluate(
                        context.chain(),
                        coverage
                )
        ).isEqualTo(
                StrategicSynthesisConfidence.MEDIUM
        );
    }

    private static StrategicEvidenceCoverage coverage(
            Context context,
            boolean unused
    ) {
        return StrategicEvidenceCoverage.of(
                context.chain(),
                List.of(
                        direct(context.finding()),
                        inherited(context.problem(), 2),
                        inherited(context.objective(), 3),
                        inherited(context.opportunity(), 4)
                )
        );
    }

    private static StrategicArtifactEvidenceSupport direct(
            StrategicArtifact artifact
    ) {
        return StrategicArtifactEvidenceSupport.direct(
                artifact,
                List.of("EVD-001"),
                TraceabilityStrength.DIRECT,
                "Direct"
        );
    }

    private static StrategicArtifactEvidenceSupport inherited(
            StrategicArtifact artifact,
            int depth
    ) {
        return StrategicArtifactEvidenceSupport.inherited(
                artifact,
                List.of("EVD-001"),
                TraceabilityStrength.STRONG,
                depth,
                "Inherited"
        );
    }

    private static StrategicArtifactEvidenceSupport weak(
            StrategicArtifact artifact,
            int depth
    ) {
        return StrategicArtifactEvidenceSupport.weak(
                artifact,
                List.of("EVD-001"),
                TraceabilityStrength.WEAK,
                depth,
                "Weak"
        );
    }

    private static Context context() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(evidence, "FND-001", StrategicArtifactType.FINDING);

        StrategicArtifact problem =
                artifact(evidence, "PRB-001", StrategicArtifactType.BUSINESS_PROBLEM);

        StrategicArtifact objective =
                artifact(evidence, "OBJ-001", StrategicArtifactType.BUSINESS_OBJECTIVE);

        StrategicArtifact opportunity =
                artifact(evidence, "OPP-001", StrategicArtifactType.STRATEGIC_OPPORTUNITY);

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        return new Context(
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

    private record Context(
            StrategicArtifact finding,
            StrategicArtifact problem,
            StrategicArtifact objective,
            StrategicArtifact opportunity,
            StrategicChain chain
    ) {
    }
}
package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicSynthesisEvidenceSummaryFactoryTest {

    private final DefaultStrategicSynthesisEvidenceSummaryFactory factory =
            new DefaultStrategicSynthesisEvidenceSummaryFactory();

    @Test
    void shouldAggregateEvidenceAndMaximumDepth() {
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

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        chain,
                        List.of(
                                StrategicArtifactEvidenceSupport.direct(
                                        finding,
                                        List.of("EVD-AUDIT-001"),
                                        TraceabilityStrength.DIRECT,
                                        "Direct"
                                ),
                                StrategicArtifactEvidenceSupport.inherited(
                                        problem,
                                        List.of("EVD-AUDIT-001"),
                                        TraceabilityStrength.STRONG,
                                        2,
                                        "Inherited"
                                ),
                                StrategicArtifactEvidenceSupport.inherited(
                                        objective,
                                        List.of(
                                                "EVD-AUDIT-001",
                                                "EVD-PROPOSAL-004"
                                        ),
                                        TraceabilityStrength.STRONG,
                                        3,
                                        "Inherited"
                                ),
                                StrategicArtifactEvidenceSupport.inherited(
                                        opportunity,
                                        List.of("EVD-PROPOSAL-004"),
                                        TraceabilityStrength.STRONG,
                                        4,
                                        "Inherited"
                                )
                        )
                );

        StrategicSynthesisEvidenceSummary result =
                factory.create(coverage);

        assertThat(result.getEvidenceCodes())
                .containsExactly(
                        "EVD-AUDIT-001",
                        "EVD-PROPOSAL-004"
                );

        assertThat(result.getMaximumTraceDepth())
                .isEqualTo(4);

        assertThat(result.getCoverageStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED
                );

        assertThat(result.getCoveragePercentage())
                .isEqualTo(100);
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
}
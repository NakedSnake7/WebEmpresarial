package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicEvidenceCoverageResponseMapperTest {

    @Test
    void shouldMapFullySupportedCoverage() {
        StrategicChain chain =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        chain,
                        List.of(
                                StrategicArtifactEvidenceSupport.direct(
                                        chain.getFinding(),
                                        List.of(
                                                "EVD-FND-001"
                                        ),
                                        TraceabilityStrength.DIRECT,
                                        "Finding directly supported"
                                ),
                                StrategicArtifactEvidenceSupport.direct(
                                        chain.getBusinessProblem(),
                                        List.of(
                                                "EVD-PRB-001"
                                        ),
                                        TraceabilityStrength.DIRECT,
                                        "Problem directly supported"
                                ),
                                StrategicArtifactEvidenceSupport.inherited(
                                        chain.getBusinessObjective(),
                                        List.of(
                                                "EVD-OBJ-001"
                                        ),
                                        TraceabilityStrength.STRONG,
                                        2,
                                        "Objective supported through traceability"
                                ),
                                StrategicArtifactEvidenceSupport.inherited(
                                        chain.getStrategicOpportunity(),
                                        List.of(
                                                "EVD-OPP-001"
                                        ),
                                        TraceabilityStrength.STRONG,
                                        2,
                                        "Opportunity supported through traceability"
                                )
                        )
                );

        StrategicEvidenceCoverageResponse response =
                StrategicEvidenceCoverageResponseMapper.toResponse(
                        coverage
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.status())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED
                );

        assertThat(response.coveragePercentage())
                .isEqualTo(
                        100
                );

        assertThat(response.supportedArtifacts())
                .isEqualTo(
                        4
                );

        assertThat(response.directArtifacts())
                .isEqualTo(
                        2
                );

        assertThat(response.weakArtifacts())
                .isZero();

        assertThat(response.unsupportedArtifacts())
                .isZero();

        assertThat(response.fullySupported())
                .isTrue();

        assertThat(response.canProceedToSynthesis())
                .isTrue();
    }

    @Test
    void shouldPreserveMostlySupportedCoverage() {
        StrategicChain chain =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        chain,
                        List.of(
                                StrategicArtifactEvidenceSupport.direct(
                                        chain.getFinding(),
                                        List.of(
                                                "EVD-FND-001"
                                        ),
                                        TraceabilityStrength.DIRECT,
                                        "Finding directly supported"
                                ),
                                StrategicArtifactEvidenceSupport.direct(
                                        chain.getBusinessProblem(),
                                        List.of(
                                                "EVD-PRB-001"
                                        ),
                                        TraceabilityStrength.DIRECT,
                                        "Problem directly supported"
                                ),
                                StrategicArtifactEvidenceSupport.inherited(
                                        chain.getBusinessObjective(),
                                        List.of(
                                                "EVD-OBJ-001"
                                        ),
                                        TraceabilityStrength.STRONG,
                                        2,
                                        "Objective supported"
                                ),
                                StrategicArtifactEvidenceSupport.weak(
                                        chain.getStrategicOpportunity(),
                                        List.of(
                                                "EVD-OPP-001"
                                        ),
                                        TraceabilityStrength.WEAK,
                                        3,
                                        "Opportunity weakly supported"
                                )
                        )
                );

        StrategicEvidenceCoverageResponse response =
                StrategicEvidenceCoverageResponseMapper.toResponse(
                        coverage
                );

        assertThat(response.status())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.MOSTLY_SUPPORTED
                );

        assertThat(response.coveragePercentage())
                .isEqualTo(
                        100
                );

        assertThat(response.supportedArtifacts())
                .isEqualTo(
                        4
                );

        assertThat(response.directArtifacts())
                .isEqualTo(
                        2
                );

        assertThat(response.weakArtifacts())
                .isEqualTo(
                        1
                );

        assertThat(response.unsupportedArtifacts())
                .isZero();

        assertThat(response.fullySupported())
                .isFalse();

        assertThat(response.canProceedToSynthesis())
                .isTrue();
    }

    @Test
    void shouldPreservePartialCoverageWithoutPromotingItToSynthesis() {
        StrategicChain chain =
                completeChain();

        StrategicEvidenceCoverage coverage =
                StrategicEvidenceCoverage.of(
                        chain,
                        List.of(
                                StrategicArtifactEvidenceSupport.direct(
                                        chain.getFinding(),
                                        List.of(
                                                "EVD-FND-001"
                                        ),
                                        TraceabilityStrength.DIRECT,
                                        "Finding directly supported"
                                ),
                                StrategicArtifactEvidenceSupport.inherited(
                                        chain.getBusinessProblem(),
                                        List.of(
                                                "EVD-PRB-001"
                                        ),
                                        TraceabilityStrength.STRONG,
                                        2,
                                        "Problem supported"
                                ),
                                StrategicArtifactEvidenceSupport.none(
                                        chain.getBusinessObjective(),
                                        "Objective unsupported"
                                ),
                                StrategicArtifactEvidenceSupport.none(
                                        chain.getStrategicOpportunity(),
                                        "Opportunity unsupported"
                                )
                        )
                );

        StrategicEvidenceCoverageResponse response =
                StrategicEvidenceCoverageResponseMapper.toResponse(
                        coverage
                );

        assertThat(response.status())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.PARTIALLY_SUPPORTED
                );

        assertThat(response.coveragePercentage())
                .isEqualTo(
                        50
                );

        assertThat(response.supportedArtifacts())
                .isEqualTo(
                        2
                );

        assertThat(response.directArtifacts())
                .isEqualTo(
                        1
                );

        assertThat(response.weakArtifacts())
                .isZero();

        assertThat(response.unsupportedArtifacts())
                .isEqualTo(
                        2
                );

        assertThat(response.fullySupported())
                .isFalse();

        assertThat(response.canProceedToSynthesis())
                .isFalse();
    }

    @Test
    void shouldReturnNullWhenCoverageIsNull() {
        assertThat(
                StrategicEvidenceCoverageResponseMapper.toResponse(
                        null
                )
        ).isNull();
    }

    private static StrategicChain completeChain() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        "Finding"
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        "Business problem"
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        "Business objective"
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        "Strategic opportunity"
                );

        return StrategicChain.of(
                evidence.getProject(),
                finding,
                problem,
                objective,
                opportunity
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
}
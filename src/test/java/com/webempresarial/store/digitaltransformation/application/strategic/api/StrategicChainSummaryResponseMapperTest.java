package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicChainSummaryResponseMapperTest {

    @Test
    void shouldMapCompleteTraversal() {
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

        StrategicTraversalResult traversal =
                StrategicTraversalResult.of(
                        StrategicTraversalStatus.COMPLETE,
                        finding,
                        problem,
                        objective,
                        opportunity,
                        List.of(),
                        List.of()
                );

        StrategicChainSummaryResponse response =
                StrategicChainSummaryResponseMapper.toResponse(
                        traversal
                );

        assertThat(response.traversalStatus())
                .isEqualTo(
                        StrategicTraversalStatus.COMPLETE
                );

        assertThat(response.complete())
                .isTrue();

        assertThat(response.ambiguous())
                .isFalse();

        assertThat(response.canBuildChain())
                .isTrue();

        assertThat(response.finding().code())
                .isEqualTo(
                        "FND-001"
                );

        assertThat(response.businessProblem().code())
                .isEqualTo(
                        "PRB-001"
                );

        assertThat(response.businessObjective().code())
                .isEqualTo(
                        "OBJ-001"
                );

        assertThat(response.strategicOpportunity().code())
                .isEqualTo(
                        "OPP-001"
                );

        assertThat(response.gaps())
                .isEmpty();

        assertThat(response.ambiguities())
                .isEmpty();
    }

    @Test
    void shouldPreserveIncompleteTraversalGap() {
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
                        "Problem"
                );

        StrategicChainGap gap =
                new StrategicChainGap(
                        StrategicChainGapType.MISSING_BUSINESS_OBJECTIVE,
                        "No existe un objetivo estratégico relacionado"
                );

        StrategicTraversalResult traversal =
                StrategicTraversalResult.of(
                        StrategicTraversalStatus.INCOMPLETE,
                        finding,
                        problem,
                        null,
                        null,
                        List.of(
                                gap
                        ),
                        List.of()
                );

        StrategicChainSummaryResponse response =
                StrategicChainSummaryResponseMapper.toResponse(
                        traversal
                );

        assertThat(response.complete())
                .isFalse();

        assertThat(response.canBuildChain())
                .isTrue();

        assertThat(response.businessObjective())
                .isNull();

        assertThat(response.strategicOpportunity())
                .isNull();

        assertThat(response.gaps())
                .singleElement()
                .satisfies(mappedGap -> {
                    assertThat(mappedGap.type())
                            .isEqualTo(
                                    StrategicChainGapType.MISSING_BUSINESS_OBJECTIVE
                            );

                    assertThat(mappedGap.description())
                            .isEqualTo(
                                    "No existe un objetivo estratégico relacionado"
                            );
                });
    }

    @Test
    void shouldPreserveAmbiguityDetails() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        "Finding"
                );

        StrategicTraversalAmbiguity ambiguity =
                new StrategicTraversalAmbiguity(
                        StrategicTraversalAmbiguityType.MULTIPLE_BUSINESS_PROBLEMS,
                        "FND-001",
                        List.of(
                                "PRB-001",
                                "PRB-002"
                        ),
                        "El finding revela más de un problema de negocio"
                );

        StrategicTraversalResult traversal =
                StrategicTraversalResult.of(
                        StrategicTraversalStatus.AMBIGUOUS,
                        finding,
                        null,
                        null,
                        null,
                        List.of(),
                        List.of(
                                ambiguity
                        )
                );

        StrategicChainSummaryResponse response =
                StrategicChainSummaryResponseMapper.toResponse(
                        traversal
                );

        assertThat(response.ambiguous())
                .isTrue();

        assertThat(response.canBuildChain())
                .isFalse();

        assertThat(response.ambiguities())
                .singleElement()
                .satisfies(mapped -> {
                    assertThat(mapped.type())
                            .isEqualTo(
                                    StrategicTraversalAmbiguityType.MULTIPLE_BUSINESS_PROBLEMS
                            );

                    assertThat(mapped.sourceArtifactCode())
                            .isEqualTo(
                                    "FND-001"
                            );

                    assertThat(mapped.candidateArtifactCodes())
                            .containsExactly(
                                    "PRB-001",
                                    "PRB-002"
                            );

                    assertThat(mapped.description())
                            .isEqualTo(
                                    "El finding revela más de un problema de negocio"
                            );
                });
    }

    @Test
    void shouldMapInvalidTraversalWithoutInventingArtifacts() {
        StrategicChainGap gap =
                new StrategicChainGap(
                        StrategicChainGapType.MISSING_FINDING,
                        "No existe un finding válido"
                );

        StrategicTraversalResult traversal =
                StrategicTraversalResult.of(
                        StrategicTraversalStatus.INVALID,
                        null,
                        null,
                        null,
                        null,
                        List.of(
                                gap
                        ),
                        List.of()
                );

        StrategicChainSummaryResponse response =
                StrategicChainSummaryResponseMapper.toResponse(
                        traversal
                );

        assertThat(response.complete())
                .isFalse();

        assertThat(response.ambiguous())
                .isFalse();

        assertThat(response.canBuildChain())
                .isFalse();

        assertThat(response.finding())
                .isNull();

        assertThat(response.businessProblem())
                .isNull();

        assertThat(response.businessObjective())
                .isNull();

        assertThat(response.strategicOpportunity())
                .isNull();
    }

    @Test
    void shouldRejectNullTraversal() {
        assertThatThrownBy(() ->
                StrategicChainSummaryResponseMapper.toResponse(
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "traversal"
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
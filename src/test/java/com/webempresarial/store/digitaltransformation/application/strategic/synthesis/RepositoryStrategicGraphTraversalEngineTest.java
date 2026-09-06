package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryStrategicGraphTraversalEngineTest {

    @Mock
    private StrategicArtifactRepository artifactRepository;

    @Mock
    private StrategicRelationshipRepository relationshipRepository;

    private RepositoryStrategicGraphTraversalEngine engine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        engine =
                new RepositoryStrategicGraphTraversalEngine(
                        artifactRepository,
                        relationshipRepository
                );
    }

    @Test
    void shouldBuildCompleteChainFromFinding() {
        Context context =
                context();

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                10L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.of(context.finding())
        );

        when(
                relationshipRepository
                        .findAllByProjectIdAndSourceArtifactIdAndStatus(
                                100L,
                                10L,
                                StrategicRelationshipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(context.reveals())
        );

        when(
                relationshipRepository
                        .findAllByProjectIdAndSourceArtifactIdAndStatus(
                                100L,
                                20L,
                                StrategicRelationshipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(context.addressedBy())
        );

        when(
                relationshipRepository
                        .findAllByProjectIdAndSourceArtifactIdAndStatus(
                                100L,
                                30L,
                                StrategicRelationshipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(context.enables())
        );

        StrategicTraversalResult result =
                engine.traverseFromFinding(
                        1L,
                        100L,
                        10L
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        StrategicTraversalStatus.COMPLETE
                );

        assertThat(result.getFinding())
                .isSameAs(context.finding());

        assertThat(result.getBusinessProblem())
                .isSameAs(context.problem());

        assertThat(result.getBusinessObjective())
                .isSameAs(context.objective());

        assertThat(result.getStrategicOpportunity())
                .isSameAs(context.opportunity());

        assertThat(result.toChain())
                .isPresent();

        assertThat(
                result.toChain()
                        .orElseThrow()
                        .getCompleteness()
        ).isEqualTo(
                StrategicChainCompleteness.COMPLETE
        );
    }

    @Test
    void shouldReturnIncompleteWhenProblemIsMissing() {
        Context context =
                context();

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                10L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.of(context.finding())
        );

        when(
                relationshipRepository
                        .findAllByProjectIdAndSourceArtifactIdAndStatus(
                                100L,
                                10L,
                                StrategicRelationshipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of()
        );

        StrategicTraversalResult result =
                engine.traverseFromFinding(
                        1L,
                        100L,
                        10L
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        StrategicTraversalStatus.INCOMPLETE
                );

        assertThat(result.getGaps())
                .extracting(
                        StrategicChainGap::type
                )
                .contains(
                        StrategicChainGapType.MISSING_BUSINESS_PROBLEM,
                        StrategicChainGapType.MISSING_BUSINESS_OBJECTIVE,
                        StrategicChainGapType.MISSING_STRATEGIC_OPPORTUNITY
                );
    }

    @Test
    void shouldReturnAmbiguousWhenFindingRevealsMultipleProblems() {
        Context context =
                context();

        StrategicArtifact secondProblem =
                artifact(
                        context.evidence(),
                        21L,
                        "PRB-002",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicRelationship secondRelationship =
                StrategicRelationship.create(
                        context.evidence().getProject(),
                        context.finding(),
                        secondProblem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        null
                );

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                10L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.of(context.finding())
        );

        when(
                relationshipRepository
                        .findAllByProjectIdAndSourceArtifactIdAndStatus(
                                100L,
                                10L,
                                StrategicRelationshipStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(
                        context.reveals(),
                        secondRelationship
                )
        );

        StrategicTraversalResult result =
                engine.traverseFromFinding(
                        1L,
                        100L,
                        10L
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        StrategicTraversalStatus.AMBIGUOUS
                );

        assertThat(result.getAmbiguities())
                .hasSize(1);

        assertThat(
                result.getAmbiguities()
                        .getFirst()
                        .type()
        ).isEqualTo(
                StrategicTraversalAmbiguityType.MULTIPLE_BUSINESS_PROBLEMS
        );

        assertThat(
                result.getAmbiguities()
                        .getFirst()
                        .candidateArtifactCodes()
        ).containsExactly(
                "PRB-001",
                "PRB-002"
        );

        assertThat(result.toChain())
                .isEmpty();
    }

    @Test
    void shouldRejectStartingArtifactThatIsNotFinding() {
        Context context =
                context();

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                20L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.of(context.problem())
        );

        assertThatThrownBy(() ->
                engine.traverseFromFinding(
                        1L,
                        100L,
                        20L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "FINDING"
                );
    }

    private static Context context() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        if (evidence.getProject().getId() == null) {
            ReflectionTestUtils.setField(
                    evidence.getProject(),
                    "id",
                    100L
            );
        }

        StrategicArtifact finding =
                artifact(
                        evidence,
                        10L,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        20L,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        30L,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        40L,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );

        StrategicRelationship reveals =
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        null
                );

        StrategicRelationship addressedBy =
                StrategicRelationship.create(
                        evidence.getProject(),
                        problem,
                        objective,
                        StrategicRelationshipType.ADDRESSED_BY,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        null
                );

        StrategicRelationship enables =
                StrategicRelationship.create(
                        evidence.getProject(),
                        objective,
                        opportunity,
                        StrategicRelationshipType.ENABLES,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        null
                );

        return new Context(
                evidence,
                finding,
                problem,
                objective,
                opportunity,
                reveals,
                addressedBy,
                enables
        );
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            Long id,
            String code,
            StrategicArtifactType type
    ) {
        StrategicArtifact artifact =
                StrategicArtifact.deriveFromEvidence(
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

        ReflectionTestUtils.setField(
                artifact,
                "id",
                id
        );

        return artifact;
    }

    private record Context(
            SourceEvidence evidence,
            StrategicArtifact finding,
            StrategicArtifact problem,
            StrategicArtifact objective,
            StrategicArtifact opportunity,
            StrategicRelationship reveals,
            StrategicRelationship addressedBy,
            StrategicRelationship enables
    ) {
    }
}
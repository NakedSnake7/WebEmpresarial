package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.application.strategic.relationship.StrategicArtifactTraceabilityNodeResolver;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryStrategicEvidenceLineageAnalyzerTest {

    @Mock
    private StrategicArtifactTraceabilityNodeResolver nodeResolver;

    @Mock
    private TraceabilityLinkRepository linkRepository;

    private RepositoryStrategicEvidenceLineageAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        analyzer =
                new RepositoryStrategicEvidenceLineageAnalyzer(
                        nodeResolver,
                        linkRepository
                );
    }

    @Test
    void shouldDetectDirectEvidenceSupport() {
        Context context =
                context();

        TraceabilityLink direct =
                link(
                        context.projectId(),
                        context.findingNode(),
                        context.evidenceNode(),
                        TraceabilityStrength.DIRECT
                );

        when(
                nodeResolver.requireNode(
                        context.finding()
                )
        ).thenReturn(
                context.findingNode()
        );

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of(direct)
        );

        StrategicArtifactEvidenceSupport support =
                analyzer.analyze(
                        context.finding()
                );

        assertThat(support.getCoverageLevel())
                .isEqualTo(
                        EvidenceCoverageLevel.DIRECT
                );

        assertThat(support.getEvidenceCodes())
                .containsExactly(
                        "EVD-AUDIT-001"
                );

        assertThat(support.getTraceDepth())
                .isEqualTo(1);

        assertThat(support.getWeakestTraceStrength())
                .isEqualTo(
                        TraceabilityStrength.DIRECT
                );
    }

    @Test
    void shouldDetectInheritedEvidenceSupport() {
        Context context =
                context();

        StrategicArtifact problem =
                artifact(
                        context.evidence(),
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        TraceabilityNode problemNode =
                node(
                        200L,
                        problem,
                        TraceabilityNodeType.BUSINESS_PROBLEM
                );

        TraceabilityLink problemToFinding =
                link(
                        context.projectId(),
                        problemNode,
                        context.findingNode(),
                        TraceabilityStrength.STRONG
                );

        TraceabilityLink findingToEvidence =
                link(
                        context.projectId(),
                        context.findingNode(),
                        context.evidenceNode(),
                        TraceabilityStrength.DIRECT
                );

        when(
                nodeResolver.requireNode(problem)
        ).thenReturn(problemNode);

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                200L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of(problemToFinding)
        );

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of(findingToEvidence)
        );

        StrategicArtifactEvidenceSupport support =
                analyzer.analyze(problem);

        assertThat(support.getCoverageLevel())
                .isEqualTo(
                        EvidenceCoverageLevel.INHERITED
                );

        assertThat(support.getTraceDepth())
                .isEqualTo(2);

        assertThat(support.getWeakestTraceStrength())
                .isEqualTo(
                        TraceabilityStrength.STRONG
                );

        assertThat(support.getEvidenceCodes())
                .containsExactly(
                        "EVD-AUDIT-001"
                );
    }

    @Test
    void shouldDowngradeSupportWhenLineageContainsWeakLink() {
        Context context =
                context();

        StrategicArtifact problem =
                artifact(
                        context.evidence(),
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        TraceabilityNode problemNode =
                node(
                        200L,
                        problem,
                        TraceabilityNodeType.BUSINESS_PROBLEM
                );

        when(
                nodeResolver.requireNode(problem)
        ).thenReturn(problemNode);

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                200L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of(
                        link(
                                context.projectId(),
                                problemNode,
                                context.findingNode(),
                                TraceabilityStrength.WEAK
                        )
                )
        );

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of(
                        link(
                                context.projectId(),
                                context.findingNode(),
                                context.evidenceNode(),
                                TraceabilityStrength.DIRECT
                        )
                )
        );

        StrategicArtifactEvidenceSupport support =
                analyzer.analyze(problem);

        assertThat(support.getCoverageLevel())
                .isEqualTo(
                        EvidenceCoverageLevel.WEAK
                );

        assertThat(support.getWeakestTraceStrength())
                .isEqualTo(
                        TraceabilityStrength.WEAK
                );

        assertThat(support.getTraceDepth())
                .isEqualTo(2);
    }

    @Test
    void shouldReturnNoneWhenNoEvidenceLineageExists() {
        Context context =
                context();

        when(
                nodeResolver.requireNode(
                        context.finding()
                )
        ).thenReturn(
                context.findingNode()
        );

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of()
        );

        StrategicArtifactEvidenceSupport support =
                analyzer.analyze(
                        context.finding()
                );

        assertThat(support.getCoverageLevel())
                .isEqualTo(
                        EvidenceCoverageLevel.NONE
                );

        assertThat(support.hasEvidence())
                .isFalse();
    }

    private static Context context() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        if (evidence.getProject().getId() == null) {
            ReflectionTestUtils.setField(
                    evidence.getProject(),
                    "id",
                    50L
            );
        }

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        TraceabilityNode findingNode =
                node(
                        100L,
                        finding,
                        TraceabilityNodeType.STRATEGIC_FINDING
                );

        TraceabilityNode evidenceNode =
                TraceabilityNode.create(
                        evidence.getProject(),
                        "NODE-EVD-AUDIT-001",
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        TraceabilityOrigin.MANUAL,
                        evidence.getStatement(),
                        null,
                        "EVD-AUDIT-001",
                        "SourceEvidence",
                        false
                );

        ReflectionTestUtils.setField(
                evidenceNode,
                "id",
                500L
        );

        return new Context(
                evidence,
                finding,
                findingNode,
                evidenceNode,
                50L
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

    private static TraceabilityNode node(
            Long id,
            StrategicArtifact artifact,
            TraceabilityNodeType type
    ) {
        TraceabilityNode node =
                TraceabilityNode.create(
                        artifact.getProject(),
                        artifact.getArtifactCode(),
                        type,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        artifact.getStatement(),
                        null,
                        artifact.getArtifactCode(),
                        "StrategicArtifact",
                        false
                );

        ReflectionTestUtils.setField(
                node,
                "id",
                id
        );

        return node;
    }

    private static TraceabilityLink link(
            Long projectId,
            TraceabilityNode source,
            TraceabilityNode target,
            TraceabilityStrength strength
    ) {
        return TraceabilityLink.create(
                source.getProject(),
                source,
                target,
                TraceabilityRelationType.DERIVED_FROM,
                strength,
                TraceabilityOrigin.SYSTEM_GENERATED,
                "Test lineage"
        );
    }

    private record Context(
            SourceEvidence evidence,
            StrategicArtifact finding,
            TraceabilityNode findingNode,
            TraceabilityNode evidenceNode,
            Long projectId
    ) {
    }
    @Test
    void shouldAggregateAndDeduplicateMultipleEvidenceSources() {
        Context context =
                context();

        TraceabilityNode secondEvidenceNode =
                TraceabilityNode.create(
                        context.evidence().getProject(),
                        "NODE-EVD-PROPOSAL-004",
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        TraceabilityOrigin.MANUAL,
                        "Proposal evidence",
                        null,
                        "EVD-PROPOSAL-004",
                        "SourceEvidence",
                        false
                );

        ReflectionTestUtils.setField(
                secondEvidenceNode,
                "id",
                501L
        );

        when(
                nodeResolver.requireNode(
                        context.finding()
                )
        ).thenReturn(
                context.findingNode()
        );

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of(
                        link(
                                context.projectId(),
                                context.findingNode(),
                                secondEvidenceNode,
                                TraceabilityStrength.DIRECT
                        ),
                        link(
                                context.projectId(),
                                context.findingNode(),
                                context.evidenceNode(),
                                TraceabilityStrength.DIRECT
                        ),
                        link(
                                context.projectId(),
                                context.findingNode(),
                                context.evidenceNode(),
                                TraceabilityStrength.DIRECT
                        )
                )
        );

        StrategicArtifactEvidenceSupport support =
                analyzer.analyze(
                        context.finding()
                );

        assertThat(support.getEvidenceCodes())
                .containsExactly(
                        "EVD-AUDIT-001",
                        "EVD-PROPOSAL-004"
                );

        assertThat(support.getCoverageLevel())
                .isEqualTo(
                        EvidenceCoverageLevel.DIRECT
                );
    }
    @Test
    void shouldStopSafelyWhenTraceabilityContainsCycle() {
        Context context =
                context();

        StrategicArtifact problem =
                artifact(
                        context.evidence(),
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        TraceabilityNode problemNode =
                node(
                        200L,
                        problem,
                        TraceabilityNodeType.BUSINESS_PROBLEM
                );

        when(
                nodeResolver.requireNode(problem)
        ).thenReturn(problemNode);

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                200L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of(
                        link(
                                context.projectId(),
                                problemNode,
                                context.findingNode(),
                                TraceabilityStrength.STRONG
                        )
                )
        );

        when(
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                context.projectId(),
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                List.of(
                        link(
                                context.projectId(),
                                context.findingNode(),
                                problemNode,
                                TraceabilityStrength.STRONG
                        )
                )
        );

        StrategicArtifactEvidenceSupport support =
                analyzer.analyze(problem);

        assertThat(support.getCoverageLevel())
                .isEqualTo(
                        EvidenceCoverageLevel.NONE
                );
    }
}
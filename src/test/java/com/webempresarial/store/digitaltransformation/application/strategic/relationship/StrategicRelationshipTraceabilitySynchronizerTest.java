package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StrategicRelationshipTraceabilitySynchronizerTest {

    @Mock
    private StrategicRelationshipTraceabilityMapper mapper;

    @Mock
    private StrategicArtifactTraceabilityNodeResolver nodeResolver;

    @Mock
    private TraceabilityLinkRepository linkRepository;

    private StrategicRelationshipTraceabilitySynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        synchronizer =
                new StrategicRelationshipTraceabilitySynchronizer(
                        mapper,
                        nodeResolver,
                        linkRepository
                );

        when(
                linkRepository.save(
                        any(TraceabilityLink.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );
    }

    @Test
    void shouldReverseDirectionForDerivedFromTraceability() {
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

        StrategicRelationship relationship =
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        "El hallazgo revela el problema"
                );

        TraceabilityNode findingNode =
                node(
                        finding,
                        TraceabilityNodeType.STRATEGIC_FINDING
                );

        TraceabilityNode problemNode =
                node(
                        problem,
                        TraceabilityNodeType.BUSINESS_PROBLEM
                );

        when(
                mapper.map(
                        StrategicRelationshipType.REVEALS
                )
        ).thenReturn(
                new StrategicRelationshipTraceabilityMapping(
                        true,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.STRONG
                )
        );

        when(
                nodeResolver.requireNode(
                        problem
                )
        ).thenReturn(
                problemNode
        );

        when(
                nodeResolver.requireNode(
                        finding
                )
        ).thenReturn(
                findingNode
        );

        when(
                linkRepository
                        .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                                evidence.getProject().getId(),
                                problemNode.getId(),
                                findingNode.getId(),
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                Optional.empty()
        );

        StrategicRelationshipTraceabilitySync result =
                synchronizer.synchronize(
                        relationship
                );

        assertThat(result.created())
                .isTrue();

        assertThat(
                result.link().getSourceNode()
        ).isSameAs(
                problemNode
        );

        assertThat(
                result.link().getTargetNode()
        ).isSameAs(
                findingNode
        );

        assertThat(
                result.link().getRelationType()
        ).isEqualTo(
                TraceabilityRelationType.DERIVED_FROM
        );
    }

    @Test
    void shouldBeIdempotentWhenTraceabilityAlreadyExists() {
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

        StrategicRelationship relationship =
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        null
                );

        TraceabilityNode findingNode =
                node(
                        finding,
                        TraceabilityNodeType.STRATEGIC_FINDING
                );

        TraceabilityNode problemNode =
                node(
                        problem,
                        TraceabilityNodeType.BUSINESS_PROBLEM
                );

        TraceabilityLink existing =
                TraceabilityLink.create(
                        evidence.getProject(),
                        problemNode,
                        findingNode,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.STRONG,
                        TraceabilityOrigin.RULE_BASED,
                        "Existing"
                );

        when(
                mapper.map(
                        StrategicRelationshipType.REVEALS
                )
        ).thenReturn(
                new StrategicRelationshipTraceabilityMapping(
                        true,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.STRONG
                )
        );

        when(
                nodeResolver.requireNode(problem)
        ).thenReturn(problemNode);

        when(
                nodeResolver.requireNode(finding)
        ).thenReturn(findingNode);

        when(
                linkRepository
                        .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                                evidence.getProject().getId(),
                                problemNode.getId(),
                                findingNode.getId(),
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                Optional.of(existing)
        );

        StrategicRelationshipTraceabilitySync result =
                synchronizer.synchronize(
                        relationship
                );

        assertThat(result.created())
                .isFalse();

        assertThat(result.link())
                .isSameAs(existing);

        verify(
                linkRepository,
                never()
        ).save(any());
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
            StrategicArtifact artifact,
            TraceabilityNodeType nodeType
    ) {
        return TraceabilityNode.create(
                artifact.getProject(),
                artifact.getArtifactCode(),
                nodeType,
                TraceabilityOrigin.SYSTEM_GENERATED,
                artifact.getStatement(),
                null,
                artifact.getArtifactCode(),
                "StrategicArtifact",
                false
        );
    }
}
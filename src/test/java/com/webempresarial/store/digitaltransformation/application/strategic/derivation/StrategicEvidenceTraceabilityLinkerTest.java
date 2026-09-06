package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StrategicEvidenceTraceabilityLinkerTest {

    @Mock
    private TraceabilityNodeRepository nodeRepository;

    @Mock
    private TraceabilityLinkRepository linkRepository;

    private StrategicEvidenceTraceabilityLinker linker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        linker =
                new StrategicEvidenceTraceabilityLinker(
                        nodeRepository,
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
    void shouldCreateDerivedFromRelationship() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                strategicArtifact(evidence);

        TraceabilityNode evidenceNode =
                evidenceNode(evidence);

        TraceabilityNode strategicNode =
                strategicNode(artifact);

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                evidence.getProject().getId(),
                                TraceabilityNodeType.SOURCE_EVIDENCE,
                                evidence.getEvidenceCode()
                        )
        ).thenReturn(
                Optional.of(evidenceNode)
        );

        when(
                linkRepository
                        .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                                artifact.getProject().getId(),
                                strategicNode.getId(),
                                evidenceNode.getId(),
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                Optional.empty()
        );

        TraceabilityLink result =
                linker.link(
                        evidence,
                        artifact,
                        strategicNode
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.getSourceNode())
                .isSameAs(strategicNode);

        assertThat(result.getTargetNode())
                .isSameAs(evidenceNode);

        assertThat(result.getRelationType())
                .isEqualTo(
                        TraceabilityRelationType.DERIVED_FROM
                );

        assertThat(result.getStrength())
                .isEqualTo(
                        TraceabilityStrength.DIRECT
                );

        assertThat(result.getOrigin())
                .isEqualTo(
                        TraceabilityOrigin.SYSTEM_GENERATED
                );

        verify(linkRepository)
                .save(
                        any(TraceabilityLink.class)
                );
    }

    @Test
    void shouldReturnExistingLinkInsteadOfCreatingDuplicate() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                strategicArtifact(evidence);

        TraceabilityNode evidenceNode =
                evidenceNode(evidence);

        TraceabilityNode strategicNode =
                strategicNode(artifact);

        TraceabilityLink existingLink =
                TraceabilityLink.create(
                        artifact.getProject(),
                        strategicNode,
                        evidenceNode,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.DIRECT,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        "Derivación existente"
                );

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                evidence.getProject().getId(),
                                TraceabilityNodeType.SOURCE_EVIDENCE,
                                evidence.getEvidenceCode()
                        )
        ).thenReturn(
                Optional.of(evidenceNode)
        );

        when(
                linkRepository
                        .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                                artifact.getProject().getId(),
                                strategicNode.getId(),
                                evidenceNode.getId(),
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                Optional.of(existingLink)
        );

        TraceabilityLink result =
                linker.link(
                        evidence,
                        artifact,
                        strategicNode
                );

        assertThat(result)
                .isSameAs(existingLink);

        verify(
                linkRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldFailWhenEvidenceTraceabilityNodeDoesNotExist() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                strategicArtifact(evidence);

        TraceabilityNode strategicNode =
                strategicNode(artifact);

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                evidence.getProject().getId(),
                                TraceabilityNodeType.SOURCE_EVIDENCE,
                                evidence.getEvidenceCode()
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                linker.link(
                        evidence,
                        artifact,
                        strategicNode
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no posee un nodo"
                );

        verifyNoInteractions(
                linkRepository
        );
    }

    private static StrategicArtifact strategicArtifact(
            SourceEvidence evidence
    ) {
        return StrategicArtifact.deriveFromEvidence(
                evidence.getProject(),
                evidence,
                "FND-001",
                StrategicArtifactType.FINDING,
                StrategicConfidence.EXPLICIT,
                StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                evidence.getStatement(),
                evidence.getInterpretation(),
                null
        );
    }

    private static TraceabilityNode evidenceNode(
            SourceEvidence evidence
    ) {
        return TraceabilityNode.create(
                evidence.getProject(),
                "NODE-EVD-AUDIT-001",
                TraceabilityNodeType.SOURCE_EVIDENCE,
                TraceabilityOrigin.MANUAL,
                evidence.getStatement(),
                null,
                evidence.getEvidenceCode(),
                "SourceEvidence",
                false
        );
    }

    private static TraceabilityNode strategicNode(
            StrategicArtifact artifact
    ) {
        return TraceabilityNode.create(
                artifact.getProject(),
                "FND-001",
                TraceabilityNodeType.STRATEGIC_FINDING,
                TraceabilityOrigin.SYSTEM_GENERATED,
                artifact.getStatement(),
                artifact.getRationale(),
                artifact.getArtifactCode(),
                "StrategicArtifact",
                false
        );
    }
}
package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.application.strategic.StrategicTraceabilityTypeMapper;
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
import static org.mockito.Mockito.*;

class StrategicArtifactTraceabilityNodeResolverTest {

    @Mock
    private TraceabilityNodeRepository nodeRepository;

    private StrategicArtifactTraceabilityNodeResolver resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        resolver =
                new StrategicArtifactTraceabilityNodeResolver(
                        nodeRepository,
                        new StrategicTraceabilityTypeMapper()
                );
    }

    @Test
    void shouldResolveStrategicNode() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                StrategicArtifact.deriveFromEvidence(
                        evidence.getProject(),
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        StrategicConfidence.EXPLICIT,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        "Finding",
                        null,
                        null
                );

        TraceabilityNode node =
                TraceabilityNode.create(
                        artifact.getProject(),
                        "FND-001",
                        TraceabilityNodeType.STRATEGIC_FINDING,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        "Finding",
                        null,
                        "FND-001",
                        "StrategicArtifact",
                        false
                );

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                artifact.getProject().getId(),
                                TraceabilityNodeType.STRATEGIC_FINDING,
                                "FND-001"
                        )
        ).thenReturn(
                Optional.of(node)
        );

        assertThat(
                resolver.requireNode(artifact)
        ).isSameAs(node);
    }

    @Test
    void shouldFailWhenNodeDoesNotExist() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                StrategicArtifact.deriveFromEvidence(
                        evidence.getProject(),
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        StrategicConfidence.EXPLICIT,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        "Finding",
                        null,
                        null
                );

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                artifact.getProject().getId(),
                                TraceabilityNodeType.STRATEGIC_FINDING,
                                "FND-001"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                resolver.requireNode(
                        artifact
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "No existe nodo"
                );
    }
}
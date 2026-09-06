package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.StrategicTraceabilityTypeMapper;
import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityNodeRegistrar;
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

class StrategicTraceabilityRegistrarTest {

    @Mock
    private TraceabilityNodeRepository nodeRepository;

    @Mock
    private TraceabilityNodeRegistrar nodeRegistrar;

    private StrategicTraceabilityRegistrar registrar;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registrar =
                new StrategicTraceabilityRegistrar(
                        nodeRepository,
                        nodeRegistrar,
                        new StrategicTraceabilityTypeMapper()
                );
    }

    @Test
    void shouldReturnExistingNodeInsteadOfCreatingDuplicate() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                strategicArtifact(evidence);

        TraceabilityNode existingNode =
                traceabilityNode(artifact);

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                artifact.getProject().getId(),
                                TraceabilityNodeType.STRATEGIC_FINDING,
                                artifact.getArtifactCode()
                        )
        ).thenReturn(
                Optional.of(existingNode)
        );

        TraceabilityNode result =
                registrar.register(
                        artifact
                );

        assertThat(result)
                .isSameAs(existingNode);

        verifyNoInteractions(
                nodeRegistrar
        );
    }

    @Test
    void shouldCreateNodeWhenStrategicNodeDoesNotExist() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                strategicArtifact(evidence);

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                artifact.getProject().getId(),
                                TraceabilityNodeType.STRATEGIC_FINDING,
                                artifact.getArtifactCode()
                        )
        ).thenReturn(
                Optional.empty()
        );

        TraceabilityNode generatedNode =
                traceabilityNode(artifact);

        when(
                nodeRegistrar.register(
                        eq(artifact.getProject()),
                        eq("FND-001"),
                        eq(TraceabilityNodeType.STRATEGIC_FINDING),
                        eq(TraceabilityOrigin.SYSTEM_GENERATED),
                        eq(artifact.getStatement()),
                        eq(artifact.getRationale()),
                        eq("FND-001"),
                        eq("StrategicArtifact"),
                        eq(artifact.isRequiresReview())
                )
        ).thenReturn(
                generatedNode
        );

        TraceabilityNode result =
                registrar.register(
                        artifact
                );

        assertThat(result)
                .isSameAs(generatedNode);

        assertThat(result.getNodeType())
                .isEqualTo(
                        TraceabilityNodeType.STRATEGIC_FINDING
                );

        verify(nodeRegistrar)
                .register(
                        eq(artifact.getProject()),
                        eq("FND-001"),
                        eq(TraceabilityNodeType.STRATEGIC_FINDING),
                        eq(TraceabilityOrigin.SYSTEM_GENERATED),
                        eq(artifact.getStatement()),
                        eq(artifact.getRationale()),
                        eq("FND-001"),
                        eq("StrategicArtifact"),
                        eq(artifact.isRequiresReview())
                );
    }

    @Test
    void shouldUseCorrectTraceabilityTypeForBusinessObjective() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                StrategicArtifact.deriveFromEvidence(
                        evidence.getProject(),
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        StrategicConfidence.STRONGLY_SUPPORTED,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        "Elevar la experiencia digital.",
                        "Objetivo derivado del diagnóstico.",
                        null
                );

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                artifact.getProject().getId(),
                                TraceabilityNodeType.BUSINESS_OBJECTIVE,
                                "OBJ-001"
                        )
        ).thenReturn(
                Optional.empty()
        );

        TraceabilityNode generated =
                TraceabilityNode.create(
                        artifact.getProject(),
                        "OBJ-001",
                        TraceabilityNodeType.BUSINESS_OBJECTIVE,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        artifact.getStatement(),
                        artifact.getRationale(),
                        "OBJ-001",
                        "StrategicArtifact",
                        artifact.isRequiresReview()
                );

        when(
                nodeRegistrar.register(
                        any(),
                        anyString(),
                        any(),
                        any(),
                        anyString(),
                        nullable(String.class),
                        anyString(),
                        anyString(),
                        anyBoolean()
                )
        ).thenReturn(generated);

        TraceabilityNode result =
                registrar.register(artifact);

        assertThat(result.getNodeType())
                .isEqualTo(
                        TraceabilityNodeType.BUSINESS_OBJECTIVE
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

    private static TraceabilityNode traceabilityNode(
            StrategicArtifact artifact
    ) {
        return TraceabilityNode.create(
                artifact.getProject(),
                artifact.getArtifactCode(),
                TraceabilityNodeType.STRATEGIC_FINDING,
                TraceabilityOrigin.SYSTEM_GENERATED,
                artifact.getStatement(),
                artifact.getRationale(),
                artifact.getArtifactCode(),
                "StrategicArtifact",
                artifact.isRequiresReview()
        );
    }
}
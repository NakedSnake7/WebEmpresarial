package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.classification.*;
import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeriveStrategicArtifactFromEvidenceServiceTest {

    @Mock
    private SourceEvidenceRepository evidenceRepository;

    @Mock
    private SourceEvidenceStrategicCandidateMapper candidateMapper;

    @Mock
    private StrategicClassificationEngine classificationEngine;

    @Mock
    private StrategicDerivationPolicy derivationPolicy;

    @Mock
    private StrategicArtifactRepository artifactRepository;

    @Mock
    private StrategicArtifactRegistrar artifactRegistrar;

    @Mock
    private StrategicTraceabilityRegistrar traceabilityRegistrar;

    @Mock
    private StrategicEvidenceTraceabilityLinker traceabilityLinker;

    @Mock
    private StrategicDerivationProvenanceRecorder provenanceRecorder;

    private DeriveStrategicArtifactFromEvidenceService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service =
                new DeriveStrategicArtifactFromEvidenceService(
                        evidenceRepository,
                        candidateMapper,
                        classificationEngine,
                        derivationPolicy,
                        artifactRepository,
                        artifactRegistrar,
                        traceabilityRegistrar,
                        traceabilityLinker,
                        provenanceRecorder
                );
    }

    @Test
    void shouldDeriveStrategicArtifactAndTraceability() {
        SourceEvidence evidence =
                verifiedEvidence();

        StrategicClassificationCandidate candidate =
                candidate(evidence);

        StrategicClassificationResult classification =
                classification();

        StrategicArtifact artifact =
                artifact(evidence);

        TraceabilityNode evidenceNode =
                evidenceNode(evidence);

        TraceabilityNode strategicNode =
                strategicNode(artifact);

        TraceabilityLink link =
                TraceabilityLink.create(
                        artifact.getProject(),
                        strategicNode,
                        evidenceNode,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.DIRECT,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        "Derivación"
                );

        when(
                evidenceRepository.findByIdAndProjectStoreId(
                        50L,
                        1L
                )
        ).thenReturn(
                Optional.of(evidence)
        );

        when(
                candidateMapper.map(evidence)
        ).thenReturn(
                candidate
        );

        when(
                classificationEngine.classify(candidate)
        ).thenReturn(
                classification
        );

        when(
                derivationPolicy.evaluate(
                        evidence,
                        classification
                )
        ).thenReturn(
                StrategicDerivationDecision.derive(
                        "Derivación permitida"
                )
        );

        when(
                artifactRepository
                        .existsBySourceEvidenceIdAndArtifactType(
                                evidence.getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                false
        );

        when(
                artifactRegistrar.register(
                        evidence,
                        classification
                )
        ).thenReturn(
                artifact
        );

        when(
                traceabilityRegistrar.register(
                        artifact
                )
        ).thenReturn(
                strategicNode
        );

        when(
                traceabilityLinker.link(
                        evidence,
                        artifact,
                        strategicNode
                )
        ).thenReturn(
                link
        );

        StrategicDerivationResult result =
                service.derive(
                        1L,
                        50L
                );

        assertThat(result.action())
                .isEqualTo(
                        StrategicDerivationAction.DERIVE
                );

        assertThat(result.created())
                .isTrue();

        assertThat(result.strategicArtifactCode())
                .isEqualTo(
                        "FND-001"
                );

        assertThat(result.strategicArtifactType())
                .isEqualTo(
                        StrategicArtifactType.FINDING
                );

        assertThat(result.traceabilityNodeCode())
                .isEqualTo(
                        "FND-001"
                );

        verify(provenanceRecorder)
                .record(
                        evidence,
                        artifact,
                        strategicNode,
                        link
                );
    }

    @Test
    void shouldNotDeriveWhenPolicyRequiresReview() {
        SourceEvidence evidence =
                verifiedEvidence();

        StrategicClassificationCandidate candidate =
                candidate(evidence);

        StrategicClassificationResult classification =
                classification();

        when(
                evidenceRepository.findByIdAndProjectStoreId(
                        50L,
                        1L
                )
        ).thenReturn(
                Optional.of(evidence)
        );

        when(
                candidateMapper.map(evidence)
        ).thenReturn(
                candidate
        );

        when(
                classificationEngine.classify(candidate)
        ).thenReturn(
                classification
        );

        when(
                derivationPolicy.evaluate(
                        evidence,
                        classification
                )
        ).thenReturn(
                StrategicDerivationDecision.review(
                        "Revisión humana requerida"
                )
        );

        StrategicDerivationResult result =
                service.derive(
                        1L,
                        50L
                );

        assertThat(result.action())
                .isEqualTo(
                        StrategicDerivationAction.REVIEW_REQUIRED
                );

        assertThat(result.created())
                .isFalse();

        verifyNoInteractions(
                artifactRegistrar,
                traceabilityRegistrar,
                traceabilityLinker,
                provenanceRecorder
        );
    }

    @Test
    void shouldBeIdempotentWhenArtifactAlreadyExists() {
        SourceEvidence evidence =
                verifiedEvidence();

        StrategicClassificationCandidate candidate =
                candidate(evidence);

        StrategicClassificationResult classification =
                classification();

        StrategicArtifact artifact =
                artifact(evidence);

        TraceabilityNode evidenceNode =
                evidenceNode(evidence);

        TraceabilityNode strategicNode =
                strategicNode(artifact);

        TraceabilityLink link =
                TraceabilityLink.create(
                        artifact.getProject(),
                        strategicNode,
                        evidenceNode,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.DIRECT,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        "Relación existente"
                );

        when(
                evidenceRepository.findByIdAndProjectStoreId(
                        50L,
                        1L
                )
        ).thenReturn(
                Optional.of(evidence)
        );

        when(candidateMapper.map(evidence))
                .thenReturn(candidate);

        when(classificationEngine.classify(candidate))
                .thenReturn(classification);

        when(
                derivationPolicy.evaluate(
                        evidence,
                        classification
                )
        ).thenReturn(
                StrategicDerivationDecision.derive(
                        "Permitido"
                )
        );

        when(
                artifactRepository
                        .existsBySourceEvidenceIdAndArtifactType(
                                evidence.getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                true
        );

        when(
                artifactRegistrar.register(
                        evidence,
                        classification
                )
        ).thenReturn(
                artifact
        );

        when(
                traceabilityRegistrar.register(
                        artifact
                )
        ).thenReturn(
                strategicNode
        );

        when(
                traceabilityLinker.link(
                        evidence,
                        artifact,
                        strategicNode
                )
        ).thenReturn(
                link
        );

        StrategicDerivationResult result =
                service.derive(
                        1L,
                        50L
                );

        assertThat(result.created())
                .isFalse();

        assertThat(result.reason())
                .contains("ya existía");

        verifyNoInteractions(
                provenanceRecorder
        );
    }

    private static SourceEvidence verifiedEvidence() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        evidence.verify(
                "tester"
        );

        return evidence;
    }

    private static StrategicClassificationCandidate candidate(
            SourceEvidence evidence
    ) {
        return new StrategicClassificationCandidate(
                evidence.getStatement(),
                evidence.getInterpretation(),
                null,
                evidence.getClassification(),
                evidence.getConfidence(),
                evidence.getExtractionOrigin()
        );
    }

    private static StrategicClassificationResult classification() {
        return new StrategicClassificationResult(
                StrategicArtifactType.FINDING,
                StrategicConfidence.EXPLICIT,
                StrategicClassificationDecision.AUTO_ACCEPT,
                10,
                2,
                "Clasificación explícita",
                List.of(),
                false,
                true
        );
    }

    private static StrategicArtifact artifact(
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
}
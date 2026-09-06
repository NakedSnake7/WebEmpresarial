package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
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

class SyncEvidenceTraceabilityServiceTest {

    @Mock
    private SourceEvidenceRepository evidenceRepository;

    @Mock
    private TraceabilityNodeRepository nodeRepository;

    @Mock
    private EvidenceTraceabilitySyncPolicy syncPolicy;

    @Mock
    private EvidenceTraceabilityDriftDetector driftDetector;

    @Mock
    private ProvenanceRecorder provenanceRecorder;

    private SyncEvidenceTraceabilityService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service =
                new SyncEvidenceTraceabilityService(
                        evidenceRepository,
                        nodeRepository,
                        syncPolicy,
                        driftDetector,
                        provenanceRecorder
                );
    }

    @Test
    void shouldReturnNoNodeWhenEvidenceWasNeverRegistered() {
        SourceEvidence evidence =
                evidence();

        when(
                evidenceRepository.findByIdAndProjectStoreId(
                        50L,
                        1L
                )
        ).thenReturn(Optional.of(evidence));

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                evidence.getProject().getId(),
                                TraceabilityNodeType.SOURCE_EVIDENCE,
                                evidence.getEvidenceCode()
                        )
        ).thenReturn(Optional.empty());

        EvidenceTraceabilitySyncResult result =
                service.synchronize(
                        1L,
                        50L,
                        "Jovani Amacende"
                );

        assertThat(result.action())
                .isEqualTo(
                        EvidenceTraceabilitySyncAction.NO_NODE
                );

        assertThat(result.changed()).isFalse();

        verifyNoInteractions(
                syncPolicy,
                driftDetector,
                provenanceRecorder
        );
    }

    @Test
    void shouldRejectNodeWhenPolicyRequiresRejection() {
        SourceEvidence evidence =
                evidence();

        evidence.reject(
                "Invalidada",
                "Jovani Amacende"
        );

        TraceabilityNode node =
                node(evidence);

        when(
                evidenceRepository.findByIdAndProjectStoreId(
                        50L,
                        1L
                )
        ).thenReturn(Optional.of(evidence));

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                any(),
                                eq(TraceabilityNodeType.SOURCE_EVIDENCE),
                                eq(evidence.getEvidenceCode())
                        )
        ).thenReturn(Optional.of(node));

        when(
                driftDetector.detect(
                        evidence,
                        node
                )
        ).thenReturn(
                EvidenceTraceabilityDrift.none()
        );

        when(
                syncPolicy.evaluate(
                        evidence,
                        node
                )
        ).thenReturn(
                EvidenceTraceabilitySyncDecision.change(
                        EvidenceTraceabilitySyncAction.REJECTED,
                        "La evidencia fue rechazada"
                )
        );

        when(nodeRepository.save(node))
        .thenReturn(node);
        EvidenceTraceabilitySyncResult result =
                service.synchronize(
                        1L,
                        50L,
                        "Jovani Amacende"
                );

        assertThat(result.changed()).isTrue();

        assertThat(node.getStatus())
                .isEqualTo(
                        TraceabilityNodeStatus.REJECTED
                );

        verify(provenanceRecorder)
                .recordNodeAction(
                        eq(evidence.getProject()),
                        eq(node),
                        eq(ProvenanceAction.REJECTED),
                        eq(TraceabilityOrigin.SYSTEM_GENERATED),
                        eq("Jovani Amacende"),
                        eq("USER"),
                        anyString(),
                        anyString()
                );
    }

    private static SourceEvidence evidence() {
        TransformationSourceDocument source =
                TestSources.validSource();

        return SourceEvidence.extract(
                source.getProject(),
                source,
                null,
                "EVD-AUDIT-001",
                EvidenceClassification.STRATEGIC_FINDING,
                EvidenceConfidence.EXPLICIT,
                EvidenceExtractionOrigin.MANUAL,
                "Hallazgo estratégico",
                "Fragmento",
                null,
                EvidenceLocator.page(2)
        );
    }

    private static TraceabilityNode node(
            SourceEvidence evidence
    ) {
        return TraceabilityNode.create(
                evidence.getProject(),
                "NODE-EVD-AUDIT-001",
                TraceabilityNodeType.SOURCE_EVIDENCE,
                TraceabilityOrigin.MANUAL,
                evidence.getStatement(),
                "Clasificación: " +
                        evidence.getClassification(),
                evidence.getEvidenceCode(),
                "SourceEvidence",
                false
        );
    }
}
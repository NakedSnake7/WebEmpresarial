package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StrategicDerivationProvenanceRecorderTest {

    @Mock
    private ProvenanceRecorder provenanceRecorder;

    private StrategicDerivationProvenanceRecorder recorder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        recorder =
                new StrategicDerivationProvenanceRecorder(
                        provenanceRecorder
                );
    }

    @Test
    void shouldRecordNodeAndLinkProvenance() {
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
                        evidence.getStatement(),
                        evidence.getInterpretation(),
                        null
                );

        TraceabilityNode evidenceNode =
                TraceabilityNode.create(
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

        TraceabilityNode strategicNode =
                TraceabilityNode.create(
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

        TraceabilityLink link =
                TraceabilityLink.create(
                        artifact.getProject(),
                        strategicNode,
                        evidenceNode,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.DIRECT,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        "Derivación estratégica"
                );

        recorder.record(
                evidence,
                artifact,
                strategicNode,
                link
        );

        verify(provenanceRecorder)
                .recordNodeAction(
                        eq(artifact.getProject()),
                        eq(strategicNode),
                        eq(ProvenanceAction.DERIVED),
                        eq(TraceabilityOrigin.SYSTEM_GENERATED),
                        eq("StrategicDerivationEngine"),
                        eq("SYSTEM"),
                        eq("StrategicDerivationEngine"),
                        contains("FND-001")
                );

        verify(provenanceRecorder)
                .recordLinkAction(
                        eq(artifact.getProject()),
                        eq(link),
                        eq(ProvenanceAction.DERIVED),
                        eq(TraceabilityOrigin.SYSTEM_GENERATED),
                        eq("StrategicDerivationEngine"),
                        eq("SYSTEM"),
                        eq("StrategicDerivationEngine"),
                        contains("DERIVED_FROM")
                );
    }
}
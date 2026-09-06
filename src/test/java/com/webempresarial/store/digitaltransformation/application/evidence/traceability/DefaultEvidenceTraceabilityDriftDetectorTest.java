package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultEvidenceTraceabilityDriftDetectorTest {

    private final DefaultEvidenceTraceabilityDriftDetector detector =
            new DefaultEvidenceTraceabilityDriftDetector();

    @Test
    void shouldNotDetectDriftWhenEvidenceAndNodeMatch() {
        Context context =
                context("Hallazgo estratégico");

        EvidenceTraceabilityDrift drift =
                detector.detect(
                        context.evidence(),
                        context.node()
                );

        assertThat(drift.detected()).isFalse();
    }

    @Test
    void shouldDetectDriftWhenStatementChanged() {
        Context context =
                context("Hallazgo anterior");

        SourceEvidence newerEvidence =
                evidence(
                        context.source(),
                        "Hallazgo actualizado"
                );

        EvidenceTraceabilityDrift drift =
                detector.detect(
                        newerEvidence,
                        context.node()
                );

        assertThat(drift.detected()).isTrue();
        assertThat(drift.titleChanged()).isTrue();
    }

    private static Context context(
            String statement
    ) {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                evidence(
                        source,
                        statement
                );

        TraceabilityNode node =
                TraceabilityNode.create(
                        source.getProject(),
                        "NODE-EVD-001",
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        TraceabilityOrigin.MANUAL,
                        statement,
                        "Clasificación: STRATEGIC_FINDING",
                        "EVD-001",
                        "SourceEvidence",
                        false
                );

        return new Context(
                source,
                evidence,
                node
        );
    }

    private static SourceEvidence evidence(
            TransformationSourceDocument source,
            String statement
    ) {
        return SourceEvidence.extract(
                source.getProject(),
                source,
                null,
                "EVD-001",
                EvidenceClassification.STRATEGIC_FINDING,
                EvidenceConfidence.EXPLICIT,
                EvidenceExtractionOrigin.MANUAL,
                statement,
                "Fragmento",
                null,
                EvidenceLocator.page(1)
        );
    }

    private record Context(
            TransformationSourceDocument source,
            SourceEvidence evidence,
            TraceabilityNode node
    ) {
    }
}
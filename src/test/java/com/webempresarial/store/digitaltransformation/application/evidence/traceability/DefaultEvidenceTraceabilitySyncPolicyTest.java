package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultEvidenceTraceabilitySyncPolicyTest {

    private final DefaultEvidenceTraceabilitySyncPolicy policy =
            new DefaultEvidenceTraceabilitySyncPolicy();

    @Test
    void shouldRejectNodeWhenEvidenceWasRejected() {
        TestContext context = context();

        context.evidence().reject(
                "La evidencia fue invalidada",
                "Jovani Amacende"
        );

        EvidenceTraceabilitySyncDecision decision =
                policy.evaluate(
                        context.evidence(),
                        context.node()
                );

        assertThat(decision.changeRequired()).isTrue();

        assertThat(decision.action())
                .isEqualTo(
                        EvidenceTraceabilitySyncAction.REJECTED
                );
    }

    @Test
    void shouldSupersedeNodeWhenEvidenceWasSuperseded() {
        TestContext context = context();

        context.evidence().verify("Jovani Amacende");
        context.evidence().supersede();

        EvidenceTraceabilitySyncDecision decision =
                policy.evaluate(
                        context.evidence(),
                        context.node()
                );

        assertThat(decision.action())
                .isEqualTo(
                        EvidenceTraceabilitySyncAction.SUPERSEDED
                );
    }

    @Test
    void shouldArchiveNodeWhenEvidenceWasArchived() {
        TestContext context = context();

        context.evidence().archive();

        EvidenceTraceabilitySyncDecision decision =
                policy.evaluate(
                        context.evidence(),
                        context.node()
                );

        assertThat(decision.action())
                .isEqualTo(
                        EvidenceTraceabilitySyncAction.ARCHIVED
                );
    }

    @Test
    void shouldNotChangeCompatibleVerifiedEvidence() {
        TestContext context = context();

        context.evidence().verify("Jovani Amacende");

        EvidenceTraceabilitySyncDecision decision =
                policy.evaluate(
                        context.evidence(),
                        context.node()
                );

        assertThat(decision.changeRequired()).isFalse();
        assertThat(decision.action())
                .isEqualTo(
                        EvidenceTraceabilitySyncAction.NO_CHANGE
                );
    }

    private static TestContext context() {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                SourceEvidence.extract(
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

        TraceabilityNode node =
                TraceabilityNode.create(
                        source.getProject(),
                        "NODE-EVD-AUDIT-001",
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        TraceabilityOrigin.MANUAL,
                        evidence.getStatement(),
                        "Clasificación: STRATEGIC_FINDING",
                        evidence.getEvidenceCode(),
                        "SourceEvidence",
                        false
                );

        return new TestContext(
                evidence,
                node
        );
    }

    private record TestContext(
            SourceEvidence evidence,
            TraceabilityNode node
    ) {
    }
}
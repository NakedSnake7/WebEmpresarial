package com.webempresarial.store.digitaltransformation.domain.traceability;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TraceabilityNodeTest {

    @Test
    void shouldCreateTraceabilityNodeInDraftStatus() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode node =
                TraceabilityNode.create(
                        project,
                        "NODE-EVD-AUDIT-001",
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        TraceabilityOrigin.MANUAL,
                        "Evidencia de brecha digital",
                        "La marca es más fuerte que la plataforma.",
                        "EVD-AUDIT-001",
                        "SourceEvidence",
                        false
                );

        assertThat(node.getStatus())
                .isEqualTo(
                        TraceabilityNodeStatus.DRAFT
                );

        assertThat(node.getNodeCode())
                .isEqualTo("NODE-EVD-AUDIT-001");

        assertThat(node.canParticipateInVerifiedTraceability())
                .isFalse();
    }

    @Test
    void shouldVerifyTraceabilityNode() {
        TraceabilityNode node =
                validNode(
                        TestSources.validProject(),
                        "NODE-001"
                );

        node.verify("Jovani Amacende");

        assertThat(node.getStatus())
                .isEqualTo(
                        TraceabilityNodeStatus.VERIFIED
                );

        assertThat(node.canParticipateInVerifiedTraceability())
                .isTrue();

        assertThat(node.isRequiresReview()).isFalse();
    }

    @Test
    void shouldRejectVerificationWithoutReviewer() {
        TraceabilityNode node =
                validNode(
                        TestSources.validProject(),
                        "NODE-001"
                );

        assertThatThrownBy(() ->
                node.verify(" ")
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining("responsable");
    }

    private static TraceabilityNode validNode(
            TransformationProject project,
            String code
    ) {
        return TraceabilityNode.create(
                project,
                code,
                TraceabilityNodeType.SOURCE_EVIDENCE,
                TraceabilityOrigin.MANUAL,
                "Nodo",
                null,
                code,
                "SourceEvidence",
                false
        );
    }
    @Test
    void shouldReturnVerifiedNodeToActiveWhenReviewIsRequired() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode node =
                TraceabilityNode.create(
                        project,
                        "NODE-001",
                        TraceabilityNodeType.OTHER,
                        TraceabilityOrigin.MANUAL,
                        "Nodo",
                        null,
                        "REF-001",
                        "Test",
                        false
                );

        node.verify("Jovani Amacende");

        assertThat(node.getStatus())
                .isEqualTo(
                        TraceabilityNodeStatus.VERIFIED
                );

        node.requireReview();

        assertThat(node.getStatus())
                .isEqualTo(
                        TraceabilityNodeStatus.ACTIVE
                );

        assertThat(node.isRequiresReview())
                .isTrue();
    }
    @Test
    void shouldSupersedeDraftNodeFromSource() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode node =
                TraceabilityNode.create(
                        project,
                        "NODE-001",
                        TraceabilityNodeType.OTHER,
                        TraceabilityOrigin.MANUAL,
                        "Nodo",
                        null,
                        "REF-001",
                        "Test",
                        false
                );

        node.markSupersededFromSource();

        assertThat(node.getStatus())
                .isEqualTo(
                        TraceabilityNodeStatus.SUPERSEDED
                );
    }
}
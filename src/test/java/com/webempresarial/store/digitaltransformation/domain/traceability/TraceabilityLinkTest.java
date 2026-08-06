package com.webempresarial.store.digitaltransformation.domain.traceability;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TraceabilityLinkTest {

    @Test
    void shouldCreateDirectRelationshipWithoutReview() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode source =
                verifiedNode(
                        project,
                        "NODE-SOURCE"
                );

        TraceabilityNode target =
                verifiedNode(
                        project,
                        "NODE-TARGET"
                );

        TraceabilityLink link =
                TraceabilityLink.create(
                        project,
                        source,
                        target,
                        TraceabilityRelationType.SUPPORTED_BY,
                        TraceabilityStrength.DIRECT,
                        TraceabilityOrigin.MANUAL,
                        "La evidencia respalda directamente " +
                        "el hallazgo."
                );

        assertThat(link.getStatus())
                .isEqualTo(
                        TraceabilityLinkStatus.PROPOSED
                );

        assertThat(link.isRequiresReview()).isFalse();
    }

    @Test
    void shouldRequireReviewForInferredRelationship() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode source =
                verifiedNode(project, "NODE-SOURCE");

        TraceabilityNode target =
                verifiedNode(project, "NODE-TARGET");

        TraceabilityLink link =
                TraceabilityLink.create(
                        project,
                        source,
                        target,
                        TraceabilityRelationType.INFERRED_FROM,
                        TraceabilityStrength.MODERATE,
                        TraceabilityOrigin.AI_ASSISTED,
                        "Relación inferida por el motor."
                );

        assertThat(link.isRequiresReview()).isTrue();
    }

    @Test
    void shouldVerifyRelationshipBetweenVerifiedNodes() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode source =
                verifiedNode(project, "NODE-SOURCE");

        TraceabilityNode target =
                verifiedNode(project, "NODE-TARGET");

        TraceabilityLink link =
                TraceabilityLink.create(
                        project,
                        source,
                        target,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.STRONG,
                        TraceabilityOrigin.MANUAL,
                        null
                );

        link.verify("Jovani Amacende");

        assertThat(link.getStatus())
                .isEqualTo(
                        TraceabilityLinkStatus.VERIFIED
                );

        assertThat(link.isVerifiedTrace()).isTrue();
    }

    @Test
    void shouldRejectRelationshipWithSameNode() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode node =
                verifiedNode(project, "NODE-001");

        assertThatThrownBy(() ->
                TraceabilityLink.create(
                        project,
                        node,
                        node,
                        TraceabilityRelationType.RELATED_TO,
                        TraceabilityStrength.DIRECT,
                        TraceabilityOrigin.MANUAL,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining("consigo mismo");
    }

    @Test
    void shouldRejectVerificationWhenNodesAreNotVerified() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode source =
                draftNode(project, "NODE-SOURCE");

        TraceabilityNode target =
                draftNode(project, "NODE-TARGET");

        TraceabilityLink link =
                TraceabilityLink.create(
                        project,
                        source,
                        target,
                        TraceabilityRelationType.RELATED_TO,
                        TraceabilityStrength.DIRECT,
                        TraceabilityOrigin.MANUAL,
                        null
                );

        assertThatThrownBy(() ->
                link.verify("Jovani Amacende")
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining("nodos");
    }

    private static TraceabilityNode verifiedNode(
            TransformationProject project,
            String code
    ) {
        TraceabilityNode node =
                draftNode(project, code);

        node.verify("Jovani Amacende");

        return node;
    }

    private static TraceabilityNode draftNode(
            TransformationProject project,
            String code
    ) {
        return TraceabilityNode.create(
                project,
                code,
                TraceabilityNodeType.OTHER,
                TraceabilityOrigin.MANUAL,
                code,
                null,
                code,
                "TestArtifact",
                false
        );
    }
}
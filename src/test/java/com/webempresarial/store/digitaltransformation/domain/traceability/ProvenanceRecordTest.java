package com.webempresarial.store.digitaltransformation.domain.traceability;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProvenanceRecordTest {

    @Test
    void shouldCreateProvenanceForNode() {
        TransformationProject project =
                TestSources.validProject();

        TraceabilityNode node =
                TraceabilityNode.create(
                        project,
                        "NODE-001",
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        TraceabilityOrigin.MANUAL,
                        "Nodo",
                        null,
                        "EVD-001",
                        "SourceEvidence",
                        false
                );

        ProvenanceRecord record =
                ProvenanceRecord.forNode(
                        project,
                        node,
                        ProvenanceAction.CREATED,
                        TraceabilityOrigin.MANUAL,
                        "Jovani Amacende",
                        "USER",
                        "test",
                        "Creación de prueba"
                );

        assertThat(record.getTraceabilityNode())
                .isSameAs(node);

        assertThat(record.getTraceabilityLink())
                .isNull();

        assertThat(record.getAction())
                .isEqualTo(ProvenanceAction.CREATED);
    }

    @Test
    void shouldRejectRecordWithoutNodeOrLink() {
        TransformationProject project =
                TestSources.validProject();

        assertThatThrownBy(() ->
                ProvenanceRecord.forNode(
                        project,
                        null,
                        ProvenanceAction.CREATED,
                        TraceabilityOrigin.MANUAL,
                        "Jovani",
                        "USER",
                        null,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining("nodo o una relación");
    }
}
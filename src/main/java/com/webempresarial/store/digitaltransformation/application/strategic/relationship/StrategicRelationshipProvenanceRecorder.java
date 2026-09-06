package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationship;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StrategicRelationshipProvenanceRecorder {

    private static final String PROCESS_REFERENCE =
            "StrategicRelationshipEngine";

    private final ProvenanceRecorder provenanceRecorder;

    public StrategicRelationshipProvenanceRecorder(
            ProvenanceRecorder provenanceRecorder
    ) {
        this.provenanceRecorder =
                Objects.requireNonNull(
                        provenanceRecorder,
                        "ProvenanceRecorder es obligatorio"
                );
    }

    public void record(
            StrategicRelationship relationship,
            TraceabilityLink link
    ) {
        Objects.requireNonNull(
                relationship,
                "La relación estratégica es obligatoria"
        );

        Objects.requireNonNull(
                link,
                "La relación de trazabilidad es obligatoria"
        );

        provenanceRecorder.recordLinkAction(
                relationship.getProject(),
                link,
                ProvenanceAction.DERIVED,
                resolveOrigin(
                        relationship
                ),
                "StrategicRelationshipEngine",
                resolveActorType(
                        relationship
                ),
                PROCESS_REFERENCE,
                buildExplanation(
                        relationship
                )
        );
    }

    private static TraceabilityOrigin resolveOrigin(
            StrategicRelationship relationship
    ) {
        return switch (relationship.getOrigin()) {

            case MANUAL ->
                    TraceabilityOrigin.MANUAL;

            case RULE_ENGINE ->
                    TraceabilityOrigin.RULE_BASED;

            case INFERENCE_ENGINE ->
                    TraceabilityOrigin.AI_ASSISTED;
        };
    }

    private static String resolveActorType(
            StrategicRelationship relationship
    ) {
        return switch (relationship.getOrigin()) {

            case MANUAL ->
                    "USER";

            case RULE_ENGINE ->
                    "SYSTEM";

            case INFERENCE_ENGINE ->
                    "AI_AGENT";
        };
    }

    private static String buildExplanation(
            StrategicRelationship relationship
    ) {
        return "La relación estratégica " +
                relationship.getRelationshipType() +
                " entre " +
                relationship.getSourceArtifact().getArtifactCode() +
                " y " +
                relationship.getTargetArtifact().getArtifactCode() +
                " fue materializada en el grafo de trazabilidad";
    }
}
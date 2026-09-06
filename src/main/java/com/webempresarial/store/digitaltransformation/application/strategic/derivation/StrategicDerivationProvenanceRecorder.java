package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StrategicDerivationProvenanceRecorder {

    private static final String PROCESS_REFERENCE =
            "StrategicDerivationEngine";

    private static final String ACTOR =
            "StrategicDerivationEngine";

    private static final String ACTOR_TYPE =
            "SYSTEM";

    private final ProvenanceRecorder provenanceRecorder;

    public StrategicDerivationProvenanceRecorder(
            ProvenanceRecorder provenanceRecorder
    ) {
        this.provenanceRecorder =
                Objects.requireNonNull(
                        provenanceRecorder,
                        "ProvenanceRecorder es obligatorio"
                );
    }

    public void record(
            SourceEvidence evidence,
            StrategicArtifact artifact,
            TraceabilityNode strategicNode,
            TraceabilityLink derivationLink
    ) {
        Objects.requireNonNull(
                evidence,
                "La evidencia es obligatoria"
        );

        Objects.requireNonNull(
                artifact,
                "El artefacto estratégico es obligatorio"
        );

        Objects.requireNonNull(
                strategicNode,
                "El nodo estratégico es obligatorio"
        );

        Objects.requireNonNull(
                derivationLink,
                "La relación de derivación es obligatoria"
        );

        provenanceRecorder.recordNodeAction(
                artifact.getProject(),
                strategicNode,
                ProvenanceAction.DERIVED,
                TraceabilityOrigin.SYSTEM_GENERATED,
                ACTOR,
                ACTOR_TYPE,
                PROCESS_REFERENCE,
                "El artefacto estratégico " +
                artifact.getArtifactCode() +
                " fue derivado desde la evidencia " +
                evidence.getEvidenceCode()
        );

        provenanceRecorder.recordLinkAction(
                artifact.getProject(),
                derivationLink,
                ProvenanceAction.DERIVED,
                TraceabilityOrigin.SYSTEM_GENERATED,
                ACTOR,
                ACTOR_TYPE,
                PROCESS_REFERENCE,
                "Se registró la relación DERIVED_FROM entre " +
                artifact.getArtifactCode() +
                " y " +
                evidence.getEvidenceCode()
        );
    }
}
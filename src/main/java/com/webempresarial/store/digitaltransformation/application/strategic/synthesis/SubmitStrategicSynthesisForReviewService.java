package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisLifecycle;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisStatus;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SubmitStrategicSynthesisForReviewService {

    private final StrategicSynthesisTraceabilityRegistrar
            traceabilityRegistrar;

    private final StrategicSynthesisGovernanceProvenanceRecorder
            provenanceRecorder;

    public SubmitStrategicSynthesisForReviewService(
            StrategicSynthesisTraceabilityRegistrar traceabilityRegistrar,
            StrategicSynthesisGovernanceProvenanceRecorder provenanceRecorder
    ) {
        this.traceabilityRegistrar =
                Objects.requireNonNull(
                        traceabilityRegistrar,
                        "StrategicSynthesisTraceabilityRegistrar es obligatorio"
                );

        this.provenanceRecorder =
                Objects.requireNonNull(
                        provenanceRecorder,
                        "StrategicSynthesisGovernanceProvenanceRecorder es obligatorio"
                );
    }

    public SubmitStrategicSynthesisResult submit(
            StrategicSynthesis synthesis
    ) {
        Objects.requireNonNull(
                synthesis,
                "La síntesis estratégica es obligatoria"
        );

        StrategicSynthesisStatus previousStatus =
                synthesis.getStatus();

        /*
         * Primero validamos la transición.
         *
         * Si es inválida, no registramos nodo ni provenance.
         */
        StrategicSynthesisStatus resultingStatus =
                StrategicSynthesisLifecycle
                        .submitForReview(
                                previousStatus
                        );

        StrategicSynthesis updated =
                synthesis.withStatus(
                        resultingStatus
                );

        /*
         * La synthesis se convierte en ciudadano
         * del Traceability Graph.
         */
        TraceabilityNode synthesisNode =
                traceabilityRegistrar.register(
                        updated
                );

        /*
         * Solo después de una transición válida y
         * un nodo válido registramos provenance.
         */
        provenanceRecorder.recordSubmission(
                updated,
                synthesisNode,
                previousStatus,
                resultingStatus
        );

        return new SubmitStrategicSynthesisResult(
                updated,
                previousStatus,
                resultingStatus
        );
    }
}
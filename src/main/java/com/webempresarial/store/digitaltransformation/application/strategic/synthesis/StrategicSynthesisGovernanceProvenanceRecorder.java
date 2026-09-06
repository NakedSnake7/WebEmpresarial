package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StrategicSynthesisGovernanceProvenanceRecorder {

    private static final String PROCESS_REFERENCE =
            "StrategicSynthesisGovernance";

    private final ProvenanceRecorder provenanceRecorder;

    public StrategicSynthesisGovernanceProvenanceRecorder(
            ProvenanceRecorder provenanceRecorder
    ) {
        this.provenanceRecorder =
                Objects.requireNonNull(
                        provenanceRecorder,
                        "ProvenanceRecorder es obligatorio"
                );
    }

    public ProvenanceRecord recordSubmission(
            StrategicSynthesis synthesis,
            TraceabilityNode synthesisNode,
            StrategicSynthesisStatus previousStatus,
            StrategicSynthesisStatus resultingStatus
    ) {
        Objects.requireNonNull(
                synthesis,
                "La síntesis es obligatoria"
        );

        Objects.requireNonNull(
                synthesisNode,
                "El nodo de síntesis es obligatorio"
        );

        Objects.requireNonNull(
                previousStatus,
                "El estado previo es obligatorio"
        );

        Objects.requireNonNull(
                resultingStatus,
                "El estado resultante es obligatorio"
        );

        ensureSynthesisNode(
                synthesis,
                synthesisNode
        );

        return provenanceRecorder.recordNodeAction(
                synthesis.getProject(),
                synthesisNode,
                ProvenanceAction.SUBMITTED_FOR_REVIEW,
                TraceabilityOrigin.SYSTEM_GENERATED,
                "StrategicSynthesisGovernance",
                "SYSTEM",
                PROCESS_REFERENCE,
                buildSubmissionExplanation(
                        synthesis,
                        previousStatus,
                        resultingStatus
                )
        );
    }

    public ProvenanceRecord recordReview(
            StrategicSynthesisReview review,
            TraceabilityNode synthesisNode
    ) {
        Objects.requireNonNull(
                review,
                "La revisión es obligatoria"
        );

        Objects.requireNonNull(
                synthesisNode,
                "El nodo de síntesis es obligatorio"
        );

        StrategicSynthesis synthesis =
                review.getSynthesis();

        ensureSynthesisNode(
                synthesis,
                synthesisNode
        );

        return provenanceRecorder.recordNodeAction(
                synthesis.getProject(),
                synthesisNode,
                resolveAction(
                        review.getDecision()
                ),
                resolveOrigin(
                        review.getReviewerType()
                ),
                review.getReviewer(),
                resolveActorType(
                        review.getReviewerType()
                ),
                PROCESS_REFERENCE,
                buildReviewExplanation(
                        review
                )
        );
    }

    private static void ensureSynthesisNode(
            StrategicSynthesis synthesis,
            TraceabilityNode node
    ) {
        node.ensureBelongsToProject(
                synthesis.getProject()
        );

        if (node.getNodeType()
                != TraceabilityNodeType.STRATEGIC_SYNTHESIS) {

            throw new IllegalArgumentException(
                    "El nodo debe ser de tipo STRATEGIC_SYNTHESIS"
            );
        }
    }

    private static ProvenanceAction resolveAction(
            StrategicSynthesisReviewDecision decision
    ) {
        return switch (decision) {
            case APPROVE ->
                    ProvenanceAction.VERIFIED;

            case REJECT ->
                    ProvenanceAction.REJECTED;
        };
    }

    private static TraceabilityOrigin resolveOrigin(
            StrategicSynthesisReviewerType reviewerType
    ) {
        return switch (reviewerType) {
            case HUMAN_CONSULTANT,
                 PROJECT_OWNER ->
                    TraceabilityOrigin.MANUAL;

            case SYSTEM ->
                    TraceabilityOrigin.SYSTEM_GENERATED;
        };
    }

    private static String resolveActorType(
            StrategicSynthesisReviewerType reviewerType
    ) {
        return switch (reviewerType) {
            case HUMAN_CONSULTANT ->
                    "HUMAN_CONSULTANT";

            case PROJECT_OWNER ->
                    "PROJECT_OWNER";

            case SYSTEM ->
                    "SYSTEM";
        };
    }

    private static String buildSubmissionExplanation(
            StrategicSynthesis synthesis,
            StrategicSynthesisStatus previousStatus,
            StrategicSynthesisStatus resultingStatus
    ) {
        return "Strategic synthesis submitted for review. " +
                "origin=" +
                synthesis.getOrigin() +
                ", confidence=" +
                synthesis.getConfidence() +
                ", previousStatus=" +
                previousStatus +
                ", resultingStatus=" +
                resultingStatus +
                ", sourceArtifacts=" +
                String.join(
                        ",",
                        synthesis.getSourceArtifactCodes()
                );
    }

    private static String buildReviewExplanation(
            StrategicSynthesisReview review
    ) {
        StrategicSynthesis synthesis =
                review.getSynthesis();

        return "Strategic synthesis review completed. " +
                "decision=" +
                review.getDecision() +
                ", reviewerType=" +
                review.getReviewerType() +
                ", previousStatus=" +
                review.getPreviousStatus() +
                ", resultingStatus=" +
                review.getResultingStatus() +
                ", synthesisOrigin=" +
                synthesis.getOrigin() +
                ", reason=" +
                review.getReason() +
                ", sourceArtifacts=" +
                String.join(
                        ",",
                        synthesis.getSourceArtifactCodes()
                );
    }
}
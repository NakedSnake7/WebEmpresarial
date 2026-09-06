package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceStatus;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultEvidenceTraceabilitySyncPolicy
        implements EvidenceTraceabilitySyncPolicy {

    @Override
    public EvidenceTraceabilitySyncDecision evaluate(
            SourceEvidence evidence,
            TraceabilityNode node
    ) {
        Objects.requireNonNull(
                evidence,
                "La evidencia es obligatoria"
        );

        Objects.requireNonNull(
                node,
                "El nodo es obligatorio"
        );

        EvidenceStatus evidenceStatus =
                evidence.getStatus();

        TraceabilityNodeStatus nodeStatus =
                node.getStatus();

        return switch (evidenceStatus) {

            case VERIFIED ->
                    evaluateVerified(nodeStatus);

            case REJECTED ->
                    evaluateRejected(nodeStatus);

            case SUPERSEDED ->
                    evaluateSuperseded(nodeStatus);

            case ARCHIVED ->
                    evaluateArchived(nodeStatus);

            case REVIEW_REQUIRED ->
                    evaluateReviewRequired(nodeStatus);

            case EXTRACTED ->
                    EvidenceTraceabilitySyncDecision.noChange(
                            "Una evidencia extraída todavía no debe " +
                            "modificar un nodo registrado"
                    );
        };
    }

    private EvidenceTraceabilitySyncDecision evaluateVerified(
            TraceabilityNodeStatus nodeStatus
    ) {
        if (nodeStatus == TraceabilityNodeStatus.REJECTED
                || nodeStatus == TraceabilityNodeStatus.SUPERSEDED
                || nodeStatus == TraceabilityNodeStatus.ARCHIVED) {
            return EvidenceTraceabilitySyncDecision.change(
                    EvidenceTraceabilitySyncAction.REVIEW_REQUIRED,
                    "La evidencia está verificada pero el nodo se " +
                    "encuentra en un estado terminal o incompatible"
            );
        }

        return EvidenceTraceabilitySyncDecision.noChange(
                "La evidencia verificada es compatible con el estado actual del nodo"
        );
    }

    private EvidenceTraceabilitySyncDecision evaluateRejected(
            TraceabilityNodeStatus nodeStatus
    ) {
        if (nodeStatus == TraceabilityNodeStatus.REJECTED) {
            return EvidenceTraceabilitySyncDecision.noChange(
                    "La evidencia y el nodo ya se encuentran rechazados"
            );
        }

        if (nodeStatus == TraceabilityNodeStatus.ARCHIVED
                || nodeStatus == TraceabilityNodeStatus.SUPERSEDED) {
            return EvidenceTraceabilitySyncDecision.noChange(
                    "El nodo ya se encuentra en un estado terminal"
            );
        }

        return EvidenceTraceabilitySyncDecision.change(
                EvidenceTraceabilitySyncAction.REJECTED,
                "La evidencia fue rechazada y el nodo debe reflejarlo"
        );
    }

    private EvidenceTraceabilitySyncDecision evaluateSuperseded(
            TraceabilityNodeStatus nodeStatus
    ) {
        if (nodeStatus == TraceabilityNodeStatus.SUPERSEDED) {
            return EvidenceTraceabilitySyncDecision.noChange(
                    "La evidencia y el nodo ya se encuentran sustituidos"
            );
        }

        if (nodeStatus == TraceabilityNodeStatus.ARCHIVED) {
            return EvidenceTraceabilitySyncDecision.noChange(
                    "El nodo ya se encuentra archivado"
            );
        }

        return EvidenceTraceabilitySyncDecision.change(
                EvidenceTraceabilitySyncAction.SUPERSEDED,
                "La evidencia fue sustituida y el nodo debe marcarse como sustituido"
        );
    }

    private EvidenceTraceabilitySyncDecision evaluateArchived(
            TraceabilityNodeStatus nodeStatus
    ) {
        if (nodeStatus == TraceabilityNodeStatus.ARCHIVED) {
            return EvidenceTraceabilitySyncDecision.noChange(
                    "La evidencia y el nodo ya se encuentran archivados"
            );
        }

        return EvidenceTraceabilitySyncDecision.change(
                EvidenceTraceabilitySyncAction.ARCHIVED,
                "La evidencia fue archivada y el nodo debe archivarse"
        );
    }

    private EvidenceTraceabilitySyncDecision evaluateReviewRequired(
            TraceabilityNodeStatus nodeStatus
    ) {
        if (nodeStatus == TraceabilityNodeStatus.REJECTED
                || nodeStatus == TraceabilityNodeStatus.SUPERSEDED
                || nodeStatus == TraceabilityNodeStatus.ARCHIVED) {
            return EvidenceTraceabilitySyncDecision.noChange(
                    "El nodo ya se encuentra en un estado terminal"
            );
        }

        return EvidenceTraceabilitySyncDecision.change(
                EvidenceTraceabilitySyncAction.REVIEW_REQUIRED,
                "La evidencia requiere revisión y el nodo debe marcarse para revisión"
        );
    }
}
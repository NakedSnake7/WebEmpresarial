package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceStatus;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidenceRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.ProvenanceAction;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeStatus;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityOrigin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class SyncEvidenceTraceabilityService
        implements EvidenceTraceabilitySynchronizer {

    private static final String PROCESS_REFERENCE =
            "SyncEvidenceTraceabilityService";

    private final SourceEvidenceRepository evidenceRepository;
    private final TraceabilityNodeRepository nodeRepository;
    private final EvidenceTraceabilitySyncPolicy syncPolicy;
    private final EvidenceTraceabilityDriftDetector driftDetector;
    private final ProvenanceRecorder provenanceRecorder;

    public SyncEvidenceTraceabilityService(
            SourceEvidenceRepository evidenceRepository,
            TraceabilityNodeRepository nodeRepository,
            EvidenceTraceabilitySyncPolicy syncPolicy,
            EvidenceTraceabilityDriftDetector driftDetector,
            ProvenanceRecorder provenanceRecorder
    ) {
        this.evidenceRepository = evidenceRepository;
        this.nodeRepository = nodeRepository;
        this.syncPolicy = syncPolicy;
        this.driftDetector = driftDetector;
        this.provenanceRecorder = provenanceRecorder;
    }

    @Override
    public EvidenceTraceabilitySyncResult synchronize(
            Long storeId,
            Long evidenceId,
            String actor
    ) {
        validateId(
                storeId,
                "El storeId debe ser válido"
        );

        validateId(
                evidenceId,
                "El evidenceId debe ser válido"
        );

        String normalizedActor =
                normalizeActor(actor);

        SourceEvidence evidence =
                evidenceRepository
                        .findByIdAndProjectStoreId(
                                evidenceId,
                                storeId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la evidencia " +
                                        evidenceId +
                                        " para el store " +
                                        storeId
                                )
                        );

        Optional<TraceabilityNode> optionalNode =
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                evidence.getProject().getId(),
                                TraceabilityNodeType.SOURCE_EVIDENCE,
                                evidence.getEvidenceCode()
                        );

        if (optionalNode.isEmpty()) {
            return new EvidenceTraceabilitySyncResult(
                    evidence.getId(),
                    evidence.getEvidenceCode(),
                    evidence.getStatus(),
                    null,
                    null,
                    null,
                    EvidenceTraceabilitySyncAction.NO_NODE,
                    false,
                    "La evidencia todavía no tiene un nodo " +
                    "de trazabilidad registrado"
            );
        }

        TraceabilityNode node =
                optionalNode.get();

        /*
         * =====================================================
         * DRIFT DETECTION
         * =====================================================
         *
         * Se evalúa antes de la política normal.
         *
         * Si una evidencia continúa VERIFIED pero su contenido
         * ya no coincide con el nodo, no podemos mantener el
         * nodo como VERIFIED silenciosamente.
         */
        EvidenceTraceabilityDrift drift =
                driftDetector.detect(
                        evidence,
                        node
                );

        if (drift.detected()
                && evidence.getStatus() == EvidenceStatus.VERIFIED) {

            node.requireReview();

            TraceabilityNode saved =
                    nodeRepository.save(node);

            provenanceRecorder.recordNodeAction(
                    evidence.getProject(),
                    saved,
                    ProvenanceAction.REVIEWED,
                    TraceabilityOrigin.SYSTEM_GENERATED,
                    normalizedActor,
                    "USER",
                    PROCESS_REFERENCE,
                    drift.reason()
            );

            return result(
                    evidence,
                    saved,
                    EvidenceTraceabilitySyncAction.REVIEW_REQUIRED,
                    true,
                    drift.reason()
            );
        }

        /*
         * =====================================================
         * STATUS SYNCHRONIZATION
         * =====================================================
         */
        EvidenceTraceabilitySyncDecision decision =
                syncPolicy.evaluate(
                        evidence,
                        node
                );

        if (!decision.changeRequired()) {
            return result(
                    evidence,
                    node,
                    decision.action(),
                    false,
                    decision.reason()
            );
        }

        applyDecision(
                evidence,
                node,
                decision,
                normalizedActor
        );

        TraceabilityNode saved =
                nodeRepository.save(node);

        recordProvenance(
                evidence,
                saved,
                decision,
                normalizedActor
        );

        return result(
                evidence,
                saved,
                decision.action(),
                true,
                decision.reason()
        );
    }

    private void applyDecision(
            SourceEvidence evidence,
            TraceabilityNode node,
            EvidenceTraceabilitySyncDecision decision,
            String actor
    ) {
        switch (decision.action()) {

            case REJECTED ->
                    node.reject(
                            buildRejectionReason(evidence),
                            actor
                    );

            case SUPERSEDED ->
                    supersedeNode(node);

            case ARCHIVED ->
                    node.archive();

            case REVIEW_REQUIRED ->
                    node.requireReview();

            case NO_NODE,
                 NO_CHANGE ->
                    throw new IllegalStateException(
                            "La acción " +
                            decision.action() +
                            " no requiere mutación"
                    );
        }
    }

    private static void supersedeNode(
            TraceabilityNode node
    ) {
        if (node.getStatus()
                == TraceabilityNodeStatus.VERIFIED) {

            node.supersede();
            return;
        }

        if (node.getStatus()
                == TraceabilityNodeStatus.DRAFT
                || node.getStatus()
                == TraceabilityNodeStatus.ACTIVE) {

            node.markSupersededFromSource();
            return;
        }

        throw new IllegalStateException(
                "El nodo no puede ser sustituido desde el estado " +
                node.getStatus()
        );
    }

    private void recordProvenance(
            SourceEvidence evidence,
            TraceabilityNode node,
            EvidenceTraceabilitySyncDecision decision,
            String actor
    ) {
        provenanceRecorder.recordNodeAction(
                evidence.getProject(),
                node,
                resolveProvenanceAction(
                        decision.action()
                ),
                TraceabilityOrigin.SYSTEM_GENERATED,
                actor,
                "USER",
                PROCESS_REFERENCE,
                decision.reason()
        );
    }

    private static ProvenanceAction resolveProvenanceAction(
            EvidenceTraceabilitySyncAction action
    ) {
        return switch (action) {

            case REJECTED ->
                    ProvenanceAction.REJECTED;

            case SUPERSEDED ->
                    ProvenanceAction.SUPERSEDED;

            case ARCHIVED ->
                    ProvenanceAction.ARCHIVED;

            case REVIEW_REQUIRED ->
                    ProvenanceAction.REVIEWED;

            case NO_NODE,
                 NO_CHANGE ->
                    throw new IllegalArgumentException(
                            "No existe acción de procedencia para " +
                            action
                    );
        };
    }

    private static EvidenceTraceabilitySyncResult result(
            SourceEvidence evidence,
            TraceabilityNode node,
            EvidenceTraceabilitySyncAction action,
            boolean changed,
            String reason
    ) {
        return new EvidenceTraceabilitySyncResult(
                evidence.getId(),
                evidence.getEvidenceCode(),
                evidence.getStatus(),
                node.getId(),
                node.getNodeCode(),
                node.getStatus(),
                action,
                changed,
                reason
        );
    }

    private static String buildRejectionReason(
            SourceEvidence evidence
    ) {
        if (evidence.getRejectionReason() != null
                && !evidence.getRejectionReason().isBlank()) {

            return "Evidencia rechazada: " +
                    evidence.getRejectionReason();
        }

        return "La evidencia fuente fue rechazada";
    }

    private static void validateId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    message
            );
        }
    }

    private static String normalizeActor(
            String actor
    ) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "El actor es obligatorio"
            );
        }

        return actor.trim();
    }
}
package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityCodeGenerator;
import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityNodeRegistrar;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidenceRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class RegisterEvidenceIntoTraceabilityService {

    private static final String PROCESS_REFERENCE =
            "RegisterEvidenceIntoTraceabilityService";

    private final SourceEvidenceRepository evidenceRepository;
    private final TraceabilityNodeRepository nodeRepository;
    private final EvidenceRegistrationPolicy registrationPolicy;
    private final TraceabilityCodeGenerator codeGenerator;
    private final TraceabilityNodeRegistrar nodeRegistrar;
    private final ProvenanceRecorder provenanceRecorder;

    public RegisterEvidenceIntoTraceabilityService(
            SourceEvidenceRepository evidenceRepository,
            TraceabilityNodeRepository nodeRepository,
            EvidenceRegistrationPolicy registrationPolicy,
            TraceabilityCodeGenerator codeGenerator,
            TraceabilityNodeRegistrar nodeRegistrar,
            ProvenanceRecorder provenanceRecorder
           
    ) {
        this.evidenceRepository = evidenceRepository;
        this.nodeRepository = nodeRepository;
        this.registrationPolicy = registrationPolicy;
        this.codeGenerator = codeGenerator;
        this.nodeRegistrar = nodeRegistrar;
        this.provenanceRecorder = provenanceRecorder;
        
    }

    public EvidenceTraceabilityRegistrationResult register(
            Long storeId,
            Long evidenceId,
            String actor
    ) {
        validateId(storeId, "El storeId debe ser válido");
        validateId(evidenceId, "El evidenceId debe ser válido");

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

        EvidenceRegistrationDecision decision =
                registrationPolicy.evaluate(evidence);

        if (!decision.register()) {
            return EvidenceTraceabilityRegistrationResult.skipped(
                    evidence.getId(),
                    evidence.getEvidenceCode(),
                    decision.reason()
            );
        }

        Optional<TraceabilityNode> existingNode =
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                evidence.getProject().getId(),
                                TraceabilityNodeType.SOURCE_EVIDENCE,
                                evidence.getEvidenceCode()
                        );

        if (existingNode.isPresent()) {
            TraceabilityNode node =
                    existingNode.get();

            return new EvidenceTraceabilityRegistrationResult(
                    evidence.getId(),
                    evidence.getEvidenceCode(),
                    true,
                    true,
                    node.getId(),
                    node.getNodeCode(),
                    node.getStatus(),
                    "La evidencia ya estaba registrada"
            );
        }

        String nodeCode =
                codeGenerator.generateForExternalReference(
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        evidence.getEvidenceCode()
                );

        TraceabilityNode node =
                nodeRegistrar.register(
                        evidence.getProject(),
                        nodeCode,
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        resolveOrigin(evidence),
                        evidence.getStatement(),
                        buildDescription(evidence),
                        evidence.getEvidenceCode(),
                        SourceEvidence.class.getSimpleName(),
                        evidence.isRequiresHumanReview()
                );

        provenanceRecorder.recordNodeAction(
                evidence.getProject(),
                node,
                ProvenanceAction.GENERATED,
                resolveOrigin(evidence),
                normalizedActor,
                resolveActorType(evidence),
                PROCESS_REFERENCE,
                "Nodo generado desde la evidencia " +
                evidence.getEvidenceCode()
        );
        


        return new EvidenceTraceabilityRegistrationResult(
                evidence.getId(),
                evidence.getEvidenceCode(),
                true,
                false,
                node.getId(),
                node.getNodeCode(),
                node.getStatus(),
                decision.reason()
        );
    }

    private static TraceabilityOrigin resolveOrigin(
            SourceEvidence evidence
    ) {
        return switch (evidence.getExtractionOrigin()) {
            case MANUAL ->
                    TraceabilityOrigin.MANUAL;

            case RULE_BASED ->
                    TraceabilityOrigin.RULE_BASED;

            case AI_ASSISTED ->
                    TraceabilityOrigin.AI_ASSISTED;

            case IMPORTED ->
                    TraceabilityOrigin.IMPORTED;

            case SYSTEM_GENERATED ->
                    TraceabilityOrigin.SYSTEM_GENERATED;
        };
    }

    private static String resolveActorType(
            SourceEvidence evidence
    ) {
        return switch (evidence.getExtractionOrigin()) {
            case MANUAL -> "USER";
            case RULE_BASED -> "SYSTEM";
            case AI_ASSISTED -> "AI_AGENT";
            case IMPORTED -> "IMPORT_PROCESS";
            case SYSTEM_GENERATED -> "SYSTEM";
        };
    }

    private static String buildDescription(
            SourceEvidence evidence
    ) {
        StringBuilder description =
                new StringBuilder();

        description.append(
                "Clasificación: "
        ).append(
                evidence.getClassification()
        );

        description.append(
                ". Confianza: "
        ).append(
                evidence.getConfidence()
        );

        if (evidence.getInterpretation() != null) {
            description.append(
                    ". Interpretación: "
            ).append(
                    evidence.getInterpretation()
            );
        }

        return description.toString();
    }

    private static void validateId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String normalizeActor(String actor) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "El actor es obligatorio"
            );
        }

        return actor.trim();
    }
}
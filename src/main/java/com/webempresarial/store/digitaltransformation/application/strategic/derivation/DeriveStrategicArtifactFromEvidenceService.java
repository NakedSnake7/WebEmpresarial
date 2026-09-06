package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.classification.*;
import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityLink;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class DeriveStrategicArtifactFromEvidenceService {

    private final SourceEvidenceRepository evidenceRepository;
    private final SourceEvidenceStrategicCandidateMapper candidateMapper;
    private final StrategicClassificationEngine classificationEngine;
    private final StrategicDerivationPolicy derivationPolicy;
    private final StrategicArtifactRepository artifactRepository;
    private final StrategicArtifactRegistrar artifactRegistrar;
    private final StrategicTraceabilityRegistrar traceabilityRegistrar;
    private final StrategicEvidenceTraceabilityLinker traceabilityLinker;
    private final StrategicDerivationProvenanceRecorder provenanceRecorder;

    public DeriveStrategicArtifactFromEvidenceService(
            SourceEvidenceRepository evidenceRepository,
            SourceEvidenceStrategicCandidateMapper candidateMapper,
            StrategicClassificationEngine classificationEngine,
            StrategicDerivationPolicy derivationPolicy,
            StrategicArtifactRepository artifactRepository,
            StrategicArtifactRegistrar artifactRegistrar,
            StrategicTraceabilityRegistrar traceabilityRegistrar,
            StrategicEvidenceTraceabilityLinker traceabilityLinker, 
            StrategicDerivationProvenanceRecorder provenanceRecorder
    ) {
        this.evidenceRepository = evidenceRepository;
        this.candidateMapper = candidateMapper;
        this.classificationEngine = classificationEngine;
        this.derivationPolicy = derivationPolicy;
        this.artifactRepository = artifactRepository;
        this.artifactRegistrar = artifactRegistrar;
        this.traceabilityRegistrar = traceabilityRegistrar;
        this.traceabilityLinker = traceabilityLinker;
		this.provenanceRecorder = provenanceRecorder;
    }

    public StrategicDerivationResult derive(
            Long storeId,
            Long evidenceId
    ) {
        validateId(
                storeId,
                "El storeId debe ser válido"
        );

        validateId(
                evidenceId,
                "El evidenceId debe ser válido"
        );

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

        StrategicClassificationResult classification =
                classificationEngine.classify(
                        candidateMapper.map(evidence)
                );

        StrategicDerivationDecision decision =
                derivationPolicy.evaluate(
                        evidence,
                        classification
                );

        if (!decision.canDerive()) {
            return rejectedOrReview(
                    evidence,
                    classification,
                    decision
            );
        }

        boolean alreadyExists =
                artifactRepository
                        .existsBySourceEvidenceIdAndArtifactType(
                                evidence.getId(),
                                classification.proposedType()
                        );

        StrategicArtifact artifact =
                artifactRegistrar.register(
                        evidence,
                        classification
                );

        TraceabilityNode strategicNode =
                traceabilityRegistrar.register(
                        artifact
                );

        TraceabilityLink derivationLink =
                traceabilityLinker.link(
                        evidence,
                        artifact,
                        strategicNode
                );

        if (!alreadyExists) {
            provenanceRecorder.record(
                    evidence,
                    artifact,
                    strategicNode,
                    derivationLink
            );
        }

        return new StrategicDerivationResult(
                evidence.getId(),
                evidence.getEvidenceCode(),
                StrategicDerivationAction.DERIVE,
                !alreadyExists,
                artifact.getId(),
                artifact.getArtifactCode(),
                artifact.getArtifactType(),
                artifact.getStatus(),
                strategicNode.getId(),
                strategicNode.getNodeCode(),
                alreadyExists
                        ? "La derivación estratégica ya existía"
                        : decision.reason()
        );
    }

    private static StrategicDerivationResult rejectedOrReview(
            SourceEvidence evidence,
            StrategicClassificationResult classification,
            StrategicDerivationDecision decision
    ) {
        return new StrategicDerivationResult(
                evidence.getId(),
                evidence.getEvidenceCode(),
                decision.action(),
                false,
                null,
                null,
                classification.proposedType(),
                null,
                null,
                null,
                decision.reason()
        );
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
}
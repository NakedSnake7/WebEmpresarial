package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClassifyEvidenceStrategicallyService {

    private final SourceEvidenceRepository evidenceRepository;
    private final SourceEvidenceStrategicCandidateMapper candidateMapper;
    private final StrategicClassificationEngine classificationEngine;

    public ClassifyEvidenceStrategicallyService(
            SourceEvidenceRepository evidenceRepository,
            SourceEvidenceStrategicCandidateMapper candidateMapper,
            StrategicClassificationEngine classificationEngine
    ) {
        this.evidenceRepository =
                evidenceRepository;

        this.candidateMapper =
                candidateMapper;

        this.classificationEngine =
                classificationEngine;
    }

    public StrategicClassificationResult classify(
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

        return classificationEngine.classify(
                candidateMapper.map(evidence)
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
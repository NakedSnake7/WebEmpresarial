package com.webempresarial.store.digitaltransformation.application.evidence;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewSourceEvidenceService {

    private final SourceEvidenceRepository evidenceRepository;

    public ReviewSourceEvidenceService(
            SourceEvidenceRepository evidenceRepository
    ) {
        this.evidenceRepository = evidenceRepository;
    }

    public SourceEvidenceResult verify(
            Long storeId,
            Long evidenceId,
            String verifiedBy
    ) {
        SourceEvidence evidence =
                requireEvidence(storeId, evidenceId);

        evidence.verify(verifiedBy);

        return SourceEvidenceResult.from(
                evidenceRepository.save(evidence)
        );
    }

    public SourceEvidenceResult reject(
            Long storeId,
            Long evidenceId,
            String reason,
            String reviewedBy
    ) {
        SourceEvidence evidence =
                requireEvidence(storeId, evidenceId);

        evidence.reject(reason, reviewedBy);

        return SourceEvidenceResult.from(
                evidenceRepository.save(evidence)
        );
    }

    public SourceEvidenceResult requireReview(
            Long storeId,
            Long evidenceId
    ) {
        SourceEvidence evidence =
                requireEvidence(storeId, evidenceId);

        evidence.requireReview();

        return SourceEvidenceResult.from(
                evidenceRepository.save(evidence)
        );
    }

    private SourceEvidence requireEvidence(
            Long storeId,
            Long evidenceId
    ) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }

        if (evidenceId == null || evidenceId <= 0) {
            throw new IllegalArgumentException(
                    "El evidenceId debe ser válido"
            );
        }

        return evidenceRepository
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
    }
}
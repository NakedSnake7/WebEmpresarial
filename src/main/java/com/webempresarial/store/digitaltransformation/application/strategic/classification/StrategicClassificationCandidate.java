package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceClassification;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceConfidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceExtractionOrigin;

public record StrategicClassificationCandidate(
        String statement,
        String rationale,
        String businessImplication,
        EvidenceClassification sourceClassification,
        EvidenceConfidence sourceConfidence,
        EvidenceExtractionOrigin sourceOrigin
) {

    public StrategicClassificationCandidate {
        if (statement == null || statement.isBlank()) {
            throw new IllegalArgumentException(
                    "La afirmación candidata es obligatoria"
            );
        }
    }
}
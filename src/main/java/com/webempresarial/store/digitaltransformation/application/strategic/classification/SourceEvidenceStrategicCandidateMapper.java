package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SourceEvidenceStrategicCandidateMapper {

    public StrategicClassificationCandidate map(
            SourceEvidence evidence
    ) {
        Objects.requireNonNull(
                evidence,
                "La evidencia es obligatoria"
        );

        return new StrategicClassificationCandidate(
                evidence.getStatement(),
                evidence.getInterpretation(),
                null,
                evidence.getClassification(),
                evidence.getConfidence(),
                evidence.getExtractionOrigin()
        );
    }
}
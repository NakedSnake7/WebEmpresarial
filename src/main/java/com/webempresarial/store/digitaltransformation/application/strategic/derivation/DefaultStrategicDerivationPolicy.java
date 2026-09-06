package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.classification.StrategicClassificationDecision;
import com.webempresarial.store.digitaltransformation.application.strategic.classification.StrategicClassificationResult;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceStatus;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultStrategicDerivationPolicy
        implements StrategicDerivationPolicy {

    @Override
    public StrategicDerivationDecision evaluate(
            SourceEvidence evidence,
            StrategicClassificationResult classification
    ) {
        Objects.requireNonNull(
                evidence,
                "La evidencia es obligatoria"
        );

        Objects.requireNonNull(
                classification,
                "La clasificación estratégica es obligatoria"
        );

        if (evidence.getStatus() != EvidenceStatus.VERIFIED) {
            return StrategicDerivationDecision.reject(
                    "Solo una evidencia VERIFIED puede producir " +
                    "inteligencia estratégica"
            );
        }

        if (classification.proposedType() == null) {
            return StrategicDerivationDecision.reject(
                    "La clasificación no produjo un tipo estratégico"
            );
        }

        if (classification.decision()
                == StrategicClassificationDecision.REJECTED) {

            return StrategicDerivationDecision.reject(
                    "La clasificación estratégica fue rechazada"
            );
        }

        if (classification.decision()
                == StrategicClassificationDecision.REVIEW_REQUIRED) {

            return StrategicDerivationDecision.review(
                    "La clasificación estratégica requiere revisión humana"
            );
        }

        if (!classification.eligibleForAutomaticDerivation()) {
            return StrategicDerivationDecision.review(
                    "La clasificación no es elegible para derivación automática"
            );
        }

        return StrategicDerivationDecision.derive(
                "La evidencia verificada y su clasificación permiten " +
                "derivación estratégica automática"
        );
    }
}
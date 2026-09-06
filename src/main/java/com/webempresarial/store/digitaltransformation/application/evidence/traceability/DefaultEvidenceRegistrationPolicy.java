package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceClassification;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceConfidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceStatus;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Component
public class DefaultEvidenceRegistrationPolicy
        implements EvidenceRegistrationPolicy {

    private static final Set<EvidenceClassification>
            STRATEGIC_CLASSIFICATIONS =
            EnumSet.of(
                    EvidenceClassification.EXECUTIVE_INTENT,
                    EvidenceClassification.BUSINESS_OBJECTIVE,
                    EvidenceClassification.BUSINESS_PROBLEM,
                    EvidenceClassification.STRATEGIC_FINDING,
                    EvidenceClassification.STRATEGIC_OPPORTUNITY,
                    EvidenceClassification.EXISTING_STRENGTH,
                    EvidenceClassification.TRANSFORMATION_PRINCIPLE,
                    EvidenceClassification.SCOPE_COMMITMENT,
                    EvidenceClassification.DELIVERABLE_COMMITMENT,
                    EvidenceClassification.CONTRACTUAL_CONSTRAINT,
                    EvidenceClassification.AUDIENCE_INSIGHT,
                    EvidenceClassification.BRAND_POSITIONING,
                    EvidenceClassification.EXPERIENCE_REQUIREMENT,
                    EvidenceClassification.DESIGN_REQUIREMENT,
                    EvidenceClassification.CONTENT_REQUIREMENT,
                    EvidenceClassification.SEO_REQUIREMENT,
                    EvidenceClassification.CONVERSION_REQUIREMENT,
                    EvidenceClassification.LOCALIZATION_REQUIREMENT,
                    EvidenceClassification.TECHNICAL_REQUIREMENT,
                    EvidenceClassification.SECURITY_REQUIREMENT,
                    EvidenceClassification.PERFORMANCE_REQUIREMENT,
                    EvidenceClassification.BUSINESS_IMPACT,
                    EvidenceClassification.PRIORITY,
                    EvidenceClassification.RISK
            );

    @Override
    public EvidenceRegistrationDecision evaluate(
            SourceEvidence evidence
    ) {
        Objects.requireNonNull(
                evidence,
                "La evidencia es obligatoria"
        );

        if (evidence.getStatus() != EvidenceStatus.VERIFIED) {
            return EvidenceRegistrationDecision.rejected(
                    "La evidencia no está verificada"
            );
        }

        if (!evidence.canGenerateRequirements()) {
            return EvidenceRegistrationDecision.rejected(
                    "La evidencia no está habilitada para generar requisitos"
            );
        }

        if (evidence.getConfidence()
                == EvidenceConfidence.UNCERTAIN) {
            return EvidenceRegistrationDecision.rejected(
                    "La evidencia tiene confianza incierta"
            );
        }

        if (!STRATEGIC_CLASSIFICATIONS.contains(
                evidence.getClassification()
        )) {
            return EvidenceRegistrationDecision.rejected(
                    "La clasificación no requiere registro estratégico"
            );
        }

        return EvidenceRegistrationDecision.approved(
                "La evidencia está verificada y tiene relevancia estratégica"
        );
    }
}
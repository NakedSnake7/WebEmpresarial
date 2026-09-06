package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceClassification;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EvidenceClassificationStrategicRule
        implements StrategicClassificationRule {

    @Override
    public List<StrategicRuleMatch> evaluate(
            StrategicClassificationCandidate candidate
    ) {
        if (candidate.sourceClassification() == null) {
            return List.of();
        }

        return switch (candidate.sourceClassification()) {

            case STRATEGIC_FINDING ->
                    direct(
                            "SCR-EVIDENCE-FINDING",
                            StrategicArtifactType.FINDING,
                            "La evidencia ya está clasificada como hallazgo estratégico"
                    );

            case BUSINESS_PROBLEM ->
                    direct(
                            "SCR-EVIDENCE-PROBLEM",
                            StrategicArtifactType.BUSINESS_PROBLEM,
                            "La evidencia ya está clasificada como problema empresarial"
                    );

            case BUSINESS_OBJECTIVE ->
                    direct(
                            "SCR-EVIDENCE-OBJECTIVE",
                            StrategicArtifactType.BUSINESS_OBJECTIVE,
                            "La evidencia ya está clasificada como objetivo empresarial"
                    );

            case STRATEGIC_OPPORTUNITY ->
                    direct(
                            "SCR-EVIDENCE-OPPORTUNITY",
                            StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                            "La evidencia ya está clasificada como oportunidad estratégica"
                    );

            case EXISTING_STRENGTH ->
                    direct(
                            "SCR-EVIDENCE-STRENGTH",
                            StrategicArtifactType.EXISTING_STRENGTH,
                            "La evidencia ya está clasificada como fortaleza existente"
                    );

            case TRANSFORMATION_PRINCIPLE ->
                    direct(
                            "SCR-EVIDENCE-PRINCIPLE",
                            StrategicArtifactType.TRANSFORMATION_PRINCIPLE,
                            "La evidencia ya está clasificada como principio de transformación"
                    );

            default ->
                    List.of();
        };
    }

    private static List<StrategicRuleMatch> direct(
            String code,
            StrategicArtifactType type,
            String explanation
    ) {
        return List.of(
                new StrategicRuleMatch(
                        code,
                        StrategicClassificationRuleType.SOURCE_CLASSIFICATION,
                        type,
                        StrategicRuleStrength.DECISIVE,
                        true,
                        explanation
                )
        );
    }
}
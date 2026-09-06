package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class TransformationPrincipleStrategicRule
        implements StrategicClassificationRule {

    @Override
    public List<StrategicRuleMatch> evaluate(
            StrategicClassificationCandidate candidate
    ) {
        String text =
                candidate.statement()
                        .toLowerCase(Locale.ROOT);

        boolean transversal =
                text.contains("toda la experiencia")
                || text.contains("todas las decisiones")
                || text.contains("principio")
                || text.contains("por diseño")
                || text.contains("debe guiar")
                || text.contains("debe estar en el centro");

        if (!transversal) {
            return List.of();
        }

        return List.of(
                new StrategicRuleMatch(
                        "SCR-PRINCIPLE-TRANSVERSAL",
                        StrategicClassificationRuleType.BUSINESS_INTENT,
                        StrategicArtifactType.TRANSFORMATION_PRINCIPLE,
                        StrategicRuleStrength.DECISIVE,
                        true,
                        "La afirmación condiciona múltiples decisiones y funciona como regla rectora"
                )
        );
    }
}
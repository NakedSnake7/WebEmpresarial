package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class FutureStateStrategicRule
        implements StrategicClassificationRule {

    @Override
    public List<StrategicRuleMatch> evaluate(
            StrategicClassificationCandidate candidate
    ) {
        String text =
                candidate.statement()
                        .toLowerCase(Locale.ROOT);

        boolean futureSignal =
                text.contains("debe ")
                || text.contains("deberá ")
                || text.contains("objetivo")
                || text.contains("queremos ")
                || text.contains("busca ")
                || text.contains("lograr ")
                || text.contains("alcanzar ");

        if (!futureSignal) {
            return List.of();
        }

        return List.of(
                new StrategicRuleMatch(
                        "SCR-FUTURE-OBJECTIVE",
                        StrategicClassificationRuleType.BUSINESS_INTENT,
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        StrategicRuleStrength.STRONG,
                        true,
                        "La afirmación expresa un estado futuro o resultado deseado"
                )
        );
    }
}
package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceConfidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultStrategicConfidenceEvaluator
        implements StrategicConfidenceEvaluator {

    @Override
    public StrategicConfidence evaluate(
            StrategicClassificationCandidate candidate,
            int winningScore,
            int competingScore,
            List<StrategicRuleMatch> matches
    ) {
        int margin =
                winningScore - competingScore;

        boolean decisiveSourceRule =
                matches.stream()
                        .anyMatch(match ->
                                match.positive()
                                && match.strength()
                                == StrategicRuleStrength.DECISIVE
                                && match.ruleType()
                                == StrategicClassificationRuleType.SOURCE_CLASSIFICATION
                        );

        if (decisiveSourceRule
                && candidate.sourceConfidence()
                == EvidenceConfidence.EXPLICIT) {
            return StrategicConfidence.EXPLICIT;
        }

        if (winningScore >= 7 && margin >= 4) {
            return StrategicConfidence.STRONGLY_SUPPORTED;
        }

        if (winningScore >= 4 && margin >= 2) {
            return StrategicConfidence.INFERRED;
        }

        if (winningScore >= 2) {
            return StrategicConfidence.WEAKLY_SUPPORTED;
        }

        return StrategicConfidence.UNCERTAIN;
    }
}
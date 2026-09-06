package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;

import java.util.List;

public record StrategicClassificationResult(
        StrategicArtifactType proposedType,
        StrategicConfidence confidence,
        StrategicClassificationDecision decision,
        int score,
        int competingScore,
        String rationale,
        List<StrategicRuleMatch> ruleMatches,
        boolean requiresHumanReview,
        boolean eligibleForAutomaticDerivation
) {

    public StrategicClassificationResult {
        ruleMatches = ruleMatches == null
                ? List.of()
                : List.copyOf(ruleMatches);
    }
}
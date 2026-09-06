package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import java.util.List;

public interface StrategicClassificationRule {

    List<StrategicRuleMatch> evaluate(
            StrategicClassificationCandidate candidate
    );
}
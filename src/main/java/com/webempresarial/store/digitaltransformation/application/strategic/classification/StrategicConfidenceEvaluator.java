package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceConfidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;

import java.util.List;

public interface StrategicConfidenceEvaluator {

    StrategicConfidence evaluate(
            StrategicClassificationCandidate candidate,
            int winningScore,
            int competingScore,
            List<StrategicRuleMatch> matches
    );
}
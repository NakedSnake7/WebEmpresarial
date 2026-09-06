package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

public interface StrategicSynthesisReviewPolicy {

    StrategicSynthesisReviewPolicyResult evaluate(
            StrategicSynthesis synthesis,
            StrategicSynthesisReviewerType reviewerType,
            StrategicSynthesisReviewDecision decision
    );
}
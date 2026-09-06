package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.ReviewStrategicSynthesisResult;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReviewDecision;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReviewerType;

public interface ReviewStrategicSynthesisCommand {

    ReviewStrategicSynthesisResult review(
            Long storeId,
            Long projectId,
            Long synthesisId,
            String reviewer,
            StrategicSynthesisReviewerType reviewerType,
            StrategicSynthesisReviewDecision decision,
            String reason
    );
}
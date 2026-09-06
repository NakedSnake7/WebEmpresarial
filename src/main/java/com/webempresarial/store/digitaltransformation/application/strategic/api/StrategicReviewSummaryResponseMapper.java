package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesisReview;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReview;

public final class StrategicReviewSummaryResponseMapper {

    private StrategicReviewSummaryResponseMapper() {
    }

    public static StrategicReviewSummaryResponse toResponse(
            StoredStrategicSynthesisReview stored
    ) {
        if (stored == null) {
            return null;
        }

        StrategicSynthesisReview review =
                stored.review();

        return new StrategicReviewSummaryResponse(
                stored.id(),
                stored.reviewedSynthesisId(),
                stored.resultingSynthesisId(),
                review.getReviewer(),
                review.getReviewerType(),
                review.getDecision(),
                review.getReason(),
                review.getReviewedAt(),
                review.getPreviousStatus(),
                review.getResultingStatus()
        );
    }
}
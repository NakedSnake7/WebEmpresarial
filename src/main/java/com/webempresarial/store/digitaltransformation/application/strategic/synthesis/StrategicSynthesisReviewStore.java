package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReview;

import java.util.List;
import java.util.Optional;

public interface StrategicSynthesisReviewStore {

    StoredStrategicSynthesisReview save(
            Long reviewedSynthesisId,
            Long resultingSynthesisId,
            StrategicSynthesisReview review
    );

    Optional<StoredStrategicSynthesisReview> findLatestBySynthesis(
            Long storeId,
            Long projectId,
            Long synthesisId
    );

    List<StoredStrategicSynthesisReview> findAllByProject(
            Long storeId,
            Long projectId
    );
}
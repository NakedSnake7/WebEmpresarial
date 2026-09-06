package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;

public interface SubmitStrategicSynthesisForReviewCommand {

    StoredStrategicSynthesis submit(
            Long storeId,
            Long projectId,
            Long synthesisId
    );
}
package com.webempresarial.store.digitaltransformation.web.strategic.governance;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReviewerType;

import java.util.Objects;

public record StrategicGovernanceActor(
        String reviewer,
        StrategicSynthesisReviewerType reviewerType
) {

    public StrategicGovernanceActor {

        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException(
                    "El reviewer es obligatorio"
            );
        }

        reviewer =
                reviewer.trim();

        reviewerType =
                Objects.requireNonNull(
                        reviewerType,
                        "El tipo de reviewer es obligatorio"
                );
    }
}
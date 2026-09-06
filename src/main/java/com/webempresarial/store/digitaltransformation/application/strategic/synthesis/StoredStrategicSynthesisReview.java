package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReview;

import java.util.Objects;

public record StoredStrategicSynthesisReview(

        Long id,

        Long reviewedSynthesisId,

        Long resultingSynthesisId,

        StrategicSynthesisReview review

) {

    public StoredStrategicSynthesisReview {

        requirePositive(
                id,
                "id"
        );

        requirePositive(
                reviewedSynthesisId,
                "reviewedSynthesisId"
        );

        requirePositive(
                resultingSynthesisId,
                "resultingSynthesisId"
        );

        review =
                Objects.requireNonNull(
                        review,
                        "La revisión estratégica es obligatoria"
                );
    }

    private static void requirePositive(
            Long value,
            String name
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    name + " debe ser válido"
            );
        }
    }
}
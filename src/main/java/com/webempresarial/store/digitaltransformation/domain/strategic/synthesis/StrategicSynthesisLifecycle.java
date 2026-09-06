package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.Objects;

public final class StrategicSynthesisLifecycle {

    private StrategicSynthesisLifecycle() {
    }

    public static StrategicSynthesisStatus submitForReview(
            StrategicSynthesisStatus currentStatus
    ) {
        Objects.requireNonNull(
                currentStatus,
                "El estado actual es obligatorio"
        );

        if (currentStatus != StrategicSynthesisStatus.READY
                && currentStatus != StrategicSynthesisStatus.DRAFT) {

            throw new IllegalStateException(
                    "Solo una síntesis READY o DRAFT " +
                    "puede enviarse a revisión"
            );
        }

        return StrategicSynthesisStatus.REQUIRES_REVIEW;
    }

    public static StrategicSynthesisStatus applyReview(
            StrategicSynthesisStatus currentStatus,
            StrategicSynthesisReviewDecision decision
    ) {
        Objects.requireNonNull(
                currentStatus,
                "El estado actual es obligatorio"
        );

        Objects.requireNonNull(
                decision,
                "La decisión de revisión es obligatoria"
        );

        if (currentStatus
                != StrategicSynthesisStatus.REQUIRES_REVIEW) {

            throw new IllegalStateException(
                    "Solo una síntesis REQUIRES_REVIEW " +
                    "puede recibir una decisión de revisión"
            );
        }

        return switch (decision) {

            case APPROVE ->
                    StrategicSynthesisStatus.APPROVED;

            case REJECT ->
                    StrategicSynthesisStatus.REJECTED;
        };
    }

    public static boolean canSubmitForReview(
            StrategicSynthesisStatus status
    ) {
        if (status == null) {
            return false;
        }

        return status == StrategicSynthesisStatus.READY
                || status == StrategicSynthesisStatus.DRAFT;
    }

    public static boolean canReview(
            StrategicSynthesisStatus status
    ) {
        return status
                == StrategicSynthesisStatus.REQUIRES_REVIEW;
    }
}
package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.time.Instant;
import java.util.Objects;

public final class StrategicSynthesisReview {

    private final StrategicSynthesis synthesis;

    private final String reviewer;

    private final StrategicSynthesisReviewerType reviewerType;

    private final StrategicSynthesisReviewDecision decision;

    private final String reason;

    private final Instant reviewedAt;

    private final StrategicSynthesisStatus previousStatus;

    private final StrategicSynthesisStatus resultingStatus;

    private StrategicSynthesisReview(
            StrategicSynthesis synthesis,
            String reviewer,
            StrategicSynthesisReviewerType reviewerType,
            StrategicSynthesisReviewDecision decision,
            String reason,
            Instant reviewedAt
    ) {
        this.synthesis =
                Objects.requireNonNull(
                        synthesis,
                        "La síntesis estratégica es obligatoria"
                );

        this.reviewer =
                requireText(
                        reviewer,
                        "El reviewer es obligatorio"
                );

        this.reviewerType =
                Objects.requireNonNull(
                        reviewerType,
                        "El tipo de reviewer es obligatorio"
                );

        this.decision =
                Objects.requireNonNull(
                        decision,
                        "La decisión de revisión es obligatoria"
                );

        this.reason =
                requireText(
                        reason,
                        "La razón de revisión es obligatoria"
                );

        this.reviewedAt =
                Objects.requireNonNull(
                        reviewedAt,
                        "La fecha de revisión es obligatoria"
                );

        this.previousStatus =
                synthesis.getStatus();

        this.resultingStatus =
                StrategicSynthesisLifecycle.applyReview(
                        previousStatus,
                        decision
                );
    }

    public static StrategicSynthesisReview record(
            StrategicSynthesis synthesis,
            String reviewer,
            StrategicSynthesisReviewerType reviewerType,
            StrategicSynthesisReviewDecision decision,
            String reason,
            Instant reviewedAt
    ) {
        return new StrategicSynthesisReview(
                synthesis,
                reviewer,
                reviewerType,
                decision,
                reason,
                reviewedAt
        );
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    message
            );
        }

        return value.trim();
    }

    public boolean approved() {
        return decision
                == StrategicSynthesisReviewDecision.APPROVE;
    }

    public boolean rejected() {
        return decision
                == StrategicSynthesisReviewDecision.REJECT;
    }

    public StrategicSynthesis getSynthesis() {
        return synthesis;
    }

    public String getReviewer() {
        return reviewer;
    }

    public StrategicSynthesisReviewerType getReviewerType() {
        return reviewerType;
    }

    public StrategicSynthesisReviewDecision getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public StrategicSynthesisStatus getPreviousStatus() {
        return previousStatus;
    }

    public StrategicSynthesisStatus getResultingStatus() {
        return resultingStatus;
    }
}
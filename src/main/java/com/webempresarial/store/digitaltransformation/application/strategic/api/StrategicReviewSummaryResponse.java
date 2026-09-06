package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import java.time.Instant;

public record StrategicReviewSummaryResponse(

        Long id,

        Long reviewedSynthesisId,

        Long resultingSynthesisId,

        String reviewer,

        StrategicSynthesisReviewerType reviewerType,

        StrategicSynthesisReviewDecision decision,

        String reason,

        Instant reviewedAt,

        StrategicSynthesisStatus previousStatus,

        StrategicSynthesisStatus resultingStatus

) {
}
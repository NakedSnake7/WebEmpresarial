package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class DefaultReviewStrategicSynthesisCommand
        implements ReviewStrategicSynthesisCommand {

    private final StrategicSynthesisStore
            synthesisStore;

    private final ReviewStrategicSynthesisService
            reviewService;

    public DefaultReviewStrategicSynthesisCommand(
            StrategicSynthesisStore synthesisStore,
            ReviewStrategicSynthesisService reviewService
    ) {
        this.synthesisStore =
                Objects.requireNonNull(
                        synthesisStore,
                        "StrategicSynthesisStore es obligatorio"
                );

        this.reviewService =
                Objects.requireNonNull(
                        reviewService,
                        "ReviewStrategicSynthesisService es obligatorio"
                );
    }

    @Override
    @Transactional
    public ReviewStrategicSynthesisResult review(
            Long storeId,
            Long projectId,
            Long synthesisId,
            String reviewer,
            StrategicSynthesisReviewerType reviewerType,
            StrategicSynthesisReviewDecision decision,
            String reason
    ) {
        requirePositive(
                storeId,
                "storeId"
        );

        requirePositive(
                projectId,
                "projectId"
        );

        requirePositive(
                synthesisId,
                "synthesisId"
        );

        StoredStrategicSynthesis stored =
                synthesisStore
                        .findSnapshot(
                                storeId,
                                projectId,
                                synthesisId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La síntesis solicitada no pertenece al proyecto"
                                )
                        );

        return Objects.requireNonNull(
                reviewService.review(
                        stored,
                        reviewer,
                        reviewerType,
                        decision,
                        reason
                ),
                "ReviewStrategicSynthesisService devolvió un resultado nulo"
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
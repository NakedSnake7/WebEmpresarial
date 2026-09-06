package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class DefaultSubmitStrategicSynthesisForReviewCommand
        implements SubmitStrategicSynthesisForReviewCommand {

    private final StrategicSynthesisStore
            synthesisStore;

    private final SubmitStrategicSynthesisForReviewService
            submitService;

    public DefaultSubmitStrategicSynthesisForReviewCommand(
            StrategicSynthesisStore synthesisStore,
            SubmitStrategicSynthesisForReviewService submitService
    ) {
        this.synthesisStore =
                Objects.requireNonNull(
                        synthesisStore,
                        "StrategicSynthesisStore es obligatorio"
                );

        this.submitService =
                Objects.requireNonNull(
                        submitService,
                        "SubmitStrategicSynthesisForReviewService es obligatorio"
                );
    }

    @Override
    @Transactional
    public StoredStrategicSynthesis submit(
            Long storeId,
            Long projectId,
            Long synthesisId
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

        SubmitStrategicSynthesisResult result =
                Objects.requireNonNull(
                        submitService.submit(
                                stored.synthesis()
                        ),
                        "SubmitStrategicSynthesisForReviewService devolvió un resultado nulo"
                );

        StrategicSynthesis updated =
                Objects.requireNonNull(
                        result.synthesis(),
                        "El submit devolvió una síntesis nula"
                );

        return Objects.requireNonNull(
                synthesisStore.saveSnapshot(
                        updated
                ),
                "StrategicSynthesisStore devolvió un snapshot nulo"
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
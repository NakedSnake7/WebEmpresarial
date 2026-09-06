package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesisReview;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicSynthesisReviewStore;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReview;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class JpaStrategicSynthesisReviewStore
        implements StrategicSynthesisReviewStore {

    private final StrategicSynthesisReviewRecordRepository
            reviewRepository;

    private final StrategicSynthesisRecordRepository
            synthesisRepository;

    public JpaStrategicSynthesisReviewStore(
            StrategicSynthesisReviewRecordRepository reviewRepository,
            StrategicSynthesisRecordRepository synthesisRepository
    ) {
        this.reviewRepository =
                Objects.requireNonNull(
                        reviewRepository,
                        "StrategicSynthesisReviewRecordRepository es obligatorio"
                );

        this.synthesisRepository =
                Objects.requireNonNull(
                        synthesisRepository,
                        "StrategicSynthesisRecordRepository es obligatorio"
                );
    }

    @Override
    @Transactional
    public StoredStrategicSynthesisReview save(
            Long reviewedSynthesisId,
            Long resultingSynthesisId,
            StrategicSynthesisReview review
    ) {
        requirePositive(
                reviewedSynthesisId,
                "reviewedSynthesisId"
        );

        requirePositive(
                resultingSynthesisId,
                "resultingSynthesisId"
        );

        Objects.requireNonNull(
                review,
                "La revisión estratégica es obligatoria"
        );

        StrategicSynthesisRecord reviewed =
                synthesisRepository
                        .findById(
                                reviewedSynthesisId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el snapshot estratégico revisado"
                                )
                        );

        StrategicSynthesisRecord resulting =
                synthesisRepository
                        .findById(
                                resultingSynthesisId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el snapshot estratégico resultante"
                                )
                        );

        StrategicSynthesisReviewRecord record =
                StrategicSynthesisReviewRecord.from(
                        reviewed,
                        resulting,
                        review
                );

        StrategicSynthesisReviewRecord saved =
                reviewRepository.saveAndFlush(
                        record
                );

        return saved.toStoredReview();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredStrategicSynthesisReview>
    findLatestBySynthesis(
            Long storeId,
            Long projectId,
            Long synthesisId
    ) {
        requirePositive(storeId, "storeId");
        requirePositive(projectId, "projectId");
        requirePositive(synthesisId, "synthesisId");

        return reviewRepository
                .findFirstByReviewedSynthesisIdAndReviewedSynthesisProjectIdAndReviewedSynthesisProjectStoreIdOrderByReviewedAtDesc(
                        synthesisId,
                        projectId,
                        storeId
                )
                .map(
                        StrategicSynthesisReviewRecord::toStoredReview
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoredStrategicSynthesisReview>
    findAllByProject(
            Long storeId,
            Long projectId
    ) {
        requirePositive(storeId, "storeId");
        requirePositive(projectId, "projectId");

        return reviewRepository
                .findAllByReviewedSynthesisProjectIdAndReviewedSynthesisProjectStoreIdOrderByReviewedAtDesc(
                        projectId,
                        storeId
                )
                .stream()
                .map(
                        StrategicSynthesisReviewRecord::toStoredReview
                )
                .toList();
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
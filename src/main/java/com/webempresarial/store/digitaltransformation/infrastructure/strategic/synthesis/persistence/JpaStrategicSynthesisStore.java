package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicSynthesisStore;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisOrigin;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class JpaStrategicSynthesisStore
        implements StrategicSynthesisStore {

    private final StrategicSynthesisRecordRepository
            repository;

    public JpaStrategicSynthesisStore(
            StrategicSynthesisRecordRepository repository
    ) {
        this.repository =
                Objects.requireNonNull(
                        repository,
                        "StrategicSynthesisRecordRepository es obligatorio"
                );
    }

    @Override
    @Transactional
    public StoredStrategicSynthesis saveSnapshot(
            StrategicSynthesis synthesis
    ) {
        Objects.requireNonNull(
                synthesis,
                "La síntesis es obligatoria"
        );

        StrategicSynthesisRecord record =
                StrategicSynthesisRecord.from(
                        synthesis
                );

        StrategicSynthesisRecord saved =
                repository.saveAndFlush(
                        record
                );

        return saved.toStoredSynthesis();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredStrategicSynthesis> findLatestSnapshot(
            Long storeId,
            Long projectId,
            StrategicSynthesisOrigin origin
    ) {
        requirePositive(
                storeId,
                "storeId"
        );

        requirePositive(
                projectId,
                "projectId"
        );

        Objects.requireNonNull(
                origin,
                "El origen es obligatorio"
        );

        return repository
                .findFirstByProjectIdAndProjectStoreIdAndOriginOrderByCreatedAtDesc(
                        projectId,
                        storeId,
                        origin
                )
                .map(
                        StrategicSynthesisRecord::toStoredSynthesis
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredStrategicSynthesis> findSnapshot(
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

        return repository
                .findByIdAndProjectIdAndProjectStoreId(
                        synthesisId,
                        projectId,
                        storeId
                )
                .map(
                        StrategicSynthesisRecord::toStoredSynthesis
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoredStrategicSynthesis> findAllSnapshots(
            Long storeId,
            Long projectId
    ) {
        requirePositive(
                storeId,
                "storeId"
        );

        requirePositive(
                projectId,
                "projectId"
        );

        return repository
                .findAllByProjectIdAndProjectStoreIdOrderByCreatedAtDesc(
                        projectId,
                        storeId
                )
                .stream()
                .map(
                        StrategicSynthesisRecord::toStoredSynthesis
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
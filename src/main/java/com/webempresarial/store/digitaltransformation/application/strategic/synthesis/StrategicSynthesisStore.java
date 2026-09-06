package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisOrigin;

import java.util.List;
import java.util.Optional;

public interface StrategicSynthesisStore {

    StoredStrategicSynthesis saveSnapshot(
            StrategicSynthesis synthesis
    );

    Optional<StoredStrategicSynthesis> findLatestSnapshot(
            Long storeId,
            Long projectId,
            StrategicSynthesisOrigin origin
    );

    Optional<StoredStrategicSynthesis> findSnapshot(
            Long storeId,
            Long projectId,
            Long synthesisId
    );

    List<StoredStrategicSynthesis> findAllSnapshots(
            Long storeId,
            Long projectId
    );
}
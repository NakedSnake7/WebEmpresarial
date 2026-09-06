package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalResult;

public interface StrategicGraphTraversalEngine {

    StrategicTraversalResult traverseFromFinding(
            Long storeId,
            Long projectId,
            Long findingArtifactId
    );
}
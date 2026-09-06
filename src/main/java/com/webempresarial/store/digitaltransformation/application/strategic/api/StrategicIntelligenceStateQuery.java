package com.webempresarial.store.digitaltransformation.application.strategic.api;

public interface StrategicIntelligenceStateQuery {

    StrategicIntelligenceStateResponse findState(
            Long storeId,
            Long projectId,
            Long findingArtifactId
    );
}
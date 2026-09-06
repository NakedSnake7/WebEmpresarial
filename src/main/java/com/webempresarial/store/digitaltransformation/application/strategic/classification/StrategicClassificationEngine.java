package com.webempresarial.store.digitaltransformation.application.strategic.classification;

public interface StrategicClassificationEngine {

    StrategicClassificationResult classify(
            StrategicClassificationCandidate candidate
    );
}
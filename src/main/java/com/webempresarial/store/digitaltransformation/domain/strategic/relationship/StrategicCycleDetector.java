package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

public interface StrategicCycleDetector {

    boolean wouldCreateCycle(
            Long projectId,
            Long sourceArtifactId,
            Long targetArtifactId
    );
}
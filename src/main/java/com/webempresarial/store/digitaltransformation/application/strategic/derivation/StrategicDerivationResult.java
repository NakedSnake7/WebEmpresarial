package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;

public record StrategicDerivationResult(
        Long evidenceId,
        String evidenceCode,

        StrategicDerivationAction action,
        boolean created,

        Long strategicArtifactId,
        String strategicArtifactCode,
        StrategicArtifactType strategicArtifactType,
        StrategicArtifactStatus strategicArtifactStatus,

        Long traceabilityNodeId,
        String traceabilityNodeCode,

        String reason
) {
}
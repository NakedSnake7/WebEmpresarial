package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisStatus;

public final class StrategicSynthesisResponseMapper {

    private StrategicSynthesisResponseMapper() {
    }

    public static StrategicSynthesisResponse toResponse(
            StoredStrategicSynthesis stored
    ) {
        if (stored == null) {
            return null;
        }

        StrategicSynthesis synthesis =
                stored.synthesis();

        return new StrategicSynthesisResponse(
                stored.id(),
                synthesis.getStrategicThesis(),
                synthesis.getConfidence(),
                synthesis.getOrigin(),
                synthesis.getStatus(),
                synthesis.getSourceArtifactCodes(),
                stored.createdAt(),
                synthesis.getStatus()
                        == StrategicSynthesisStatus.REQUIRES_REVIEW,
                synthesis.getStatus()
                        == StrategicSynthesisStatus.APPROVED,
                synthesis.getStatus()
                        == StrategicSynthesisStatus.REJECTED
        );
    }
}
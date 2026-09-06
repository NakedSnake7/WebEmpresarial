package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;

import java.util.List;

record EvidenceLineagePath(
        List<String> evidenceCodes,
        TraceabilityStrength weakestStrength,
        int depth,
        boolean weak
) {

    EvidenceLineagePath {
        evidenceCodes =
                evidenceCodes == null
                        ? List.of()
                        : List.copyOf(evidenceCodes);

        if (depth < 0) {
            throw new IllegalArgumentException(
                    "La profundidad del lineage no puede ser negativa"
            );
        }
    }

    static EvidenceLineagePath none() {
        return new EvidenceLineagePath(
                List.of(),
                null,
                0,
                false
        );
    }

    boolean hasEvidence() {
        return !evidenceCodes.isEmpty();
    }
}
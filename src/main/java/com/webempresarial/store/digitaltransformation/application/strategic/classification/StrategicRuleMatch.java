package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;

import java.util.Objects;

public record StrategicRuleMatch(
        String ruleCode,
        StrategicClassificationRuleType ruleType,
        StrategicArtifactType suggestedType,
        StrategicRuleStrength strength,
        boolean positive,
        String explanation
) {

    public StrategicRuleMatch {
        if (ruleCode == null || ruleCode.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de regla es obligatorio"
            );
        }

        Objects.requireNonNull(
                ruleType,
                "El tipo de regla es obligatorio"
        );

        Objects.requireNonNull(
                suggestedType,
                "El tipo estratégico sugerido es obligatorio"
        );

        Objects.requireNonNull(
                strength,
                "La fuerza de regla es obligatoria"
        );

        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException(
                    "La explicación de la regla es obligatoria"
            );
        }
    }

    public int signedWeight() {
        return positive
                ? strength.weight()
                : -strength.weight();
    }
}
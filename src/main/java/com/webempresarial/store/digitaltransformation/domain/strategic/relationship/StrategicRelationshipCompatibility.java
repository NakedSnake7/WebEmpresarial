package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;

import java.util.Objects;

public final class StrategicRelationshipCompatibility {

    private StrategicRelationshipCompatibility() {
    }

    public static boolean supports(
            StrategicArtifactType sourceType,
            StrategicArtifactType targetType,
            StrategicRelationshipType relationshipType
    ) {
        Objects.requireNonNull(
                sourceType,
                "El tipo estratégico origen es obligatorio"
        );

        Objects.requireNonNull(
                targetType,
                "El tipo estratégico destino es obligatorio"
        );

        Objects.requireNonNull(
                relationshipType,
                "El tipo de relación estratégica es obligatorio"
        );

        return switch (relationshipType) {

            case REVEALS ->
                    sourceType == StrategicArtifactType.FINDING
                    && targetType
                    == StrategicArtifactType.BUSINESS_PROBLEM;

            case ADDRESSED_BY ->
                    sourceType
                    == StrategicArtifactType.BUSINESS_PROBLEM
                    && targetType
                    == StrategicArtifactType.BUSINESS_OBJECTIVE;

            case ENABLES ->
                    sourceType
                    == StrategicArtifactType.BUSINESS_OBJECTIVE
                    && targetType
                    == StrategicArtifactType.STRATEGIC_OPPORTUNITY;
        };
    }

    public static void ensureSupported(
            StrategicArtifactType sourceType,
            StrategicArtifactType targetType,
            StrategicRelationshipType relationshipType
    ) {
        if (!supports(
                sourceType,
                targetType,
                relationshipType
        )) {
            throw new IllegalArgumentException(
                    "La relación estratégica " +
                    relationshipType +
                    " no es válida entre " +
                    sourceType +
                    " y " +
                    targetType
            );
        }
    }
}
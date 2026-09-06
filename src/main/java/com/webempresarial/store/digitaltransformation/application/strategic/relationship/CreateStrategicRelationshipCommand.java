package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipOrigin;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipType;

import java.util.Objects;

public record CreateStrategicRelationshipCommand(
        Long storeId,
        Long projectId,
        Long sourceArtifactId,
        Long targetArtifactId,
        StrategicRelationshipType relationshipType,
        StrategicRelationshipOrigin origin,
        String rationale
) {

    public CreateStrategicRelationshipCommand {
        requireValidId(storeId, "El storeId debe ser válido");
        requireValidId(projectId, "El projectId debe ser válido");
        requireValidId(
                sourceArtifactId,
                "El sourceArtifactId debe ser válido"
        );
        requireValidId(
                targetArtifactId,
                "El targetArtifactId debe ser válido"
        );

        Objects.requireNonNull(
                relationshipType,
                "El tipo de relación es obligatorio"
        );

        Objects.requireNonNull(
                origin,
                "El origen de la relación es obligatorio"
        );

        if (sourceArtifactId.equals(targetArtifactId)) {
            throw new IllegalArgumentException(
                    "Los artefactos origen y destino deben ser diferentes"
            );
        }

        if (rationale != null) {
            rationale = rationale.trim();

            if (rationale.isEmpty()) {
                rationale = null;
            }
        }
    }

    private static void requireValidId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
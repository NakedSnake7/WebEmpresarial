package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultStrategicRelationshipPolicy
        implements StrategicRelationshipPolicy {

    @Override
    public void validate(
            StrategicArtifact source,
            StrategicArtifact target,
            StrategicRelationshipType relationshipType
    ) {
        Objects.requireNonNull(
                source,
                "El artefacto origen es obligatorio"
        );

        Objects.requireNonNull(
                target,
                "El artefacto destino es obligatorio"
        );

        StrategicRelationshipCompatibility
                .ensureSupported(
                        source.getArtifactType(),
                        target.getArtifactType(),
                        relationshipType
                );

        if (source.getProject() != target.getProject()) {

            Long sourceProjectId =
                    source.getProject().getId();

            Long targetProjectId =
                    target.getProject().getId();

            if (sourceProjectId == null
                    || targetProjectId == null
                    || !sourceProjectId.equals(
                            targetProjectId
                    )) {

                throw new IllegalArgumentException(
                        "Los artefactos estratégicos deben " +
                        "pertenecer al mismo proyecto"
                );
            }
        }
    }
}
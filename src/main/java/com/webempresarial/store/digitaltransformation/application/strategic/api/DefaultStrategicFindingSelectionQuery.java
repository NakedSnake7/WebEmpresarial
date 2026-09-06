package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactRepository;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DefaultStrategicFindingSelectionQuery
        implements StrategicFindingSelectionQuery {

    private final StrategicArtifactRepository
            artifactRepository;

    public DefaultStrategicFindingSelectionQuery(
            StrategicArtifactRepository artifactRepository
    ) {
        this.artifactRepository =
                Objects.requireNonNull(
                        artifactRepository,
                        "StrategicArtifactRepository es obligatorio"
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StrategicFindingOptionResponse> findAvailableFindings(
            Long storeId,
            Long projectId
    ) {
        requirePositive(
                storeId,
                "storeId"
        );

        requirePositive(
                projectId,
                "projectId"
        );

        List<StrategicArtifact> findings =
                Objects.requireNonNull(
                        artifactRepository
                                .findAllByProjectIdAndProjectStoreIdAndArtifactTypeOrderByCreatedAtAsc(
                                        projectId,
                                        storeId,
                                        StrategicArtifactType.FINDING
                                ),
                        "StrategicArtifactRepository devolvió una lista nula"
                );

        return findings.stream()
                .map(
                        DefaultStrategicFindingSelectionQuery::toResponse
                )
                .toList();
    }

    private static StrategicFindingOptionResponse toResponse(
            StrategicArtifact artifact
    ) {
        Objects.requireNonNull(
                artifact,
                "El finding estratégico es obligatorio"
        );

        if (artifact.getArtifactType()
                != StrategicArtifactType.FINDING) {

            throw new IllegalStateException(
                    "El repository devolvió un artefacto que no es FINDING"
            );
        }

        return new StrategicFindingOptionResponse(
                artifact.getId(),
                artifact.getArtifactCode(),
                artifact.getStatement(),
                artifact.getStatus(),
                artifact.getConfidence(),
                artifact.isRequiresReview()
        );
    }

    private static void requirePositive(
            Long value,
            String name
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    name + " debe ser válido"
            );
        }
    }
}
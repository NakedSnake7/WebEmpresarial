package com.webempresarial.store.digitaltransformation.application.project.api;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DefaultTransformationProjectSelectionQuery
        implements TransformationProjectSelectionQuery {

    private final TransformationProjectRepository
            projectRepository;

    public DefaultTransformationProjectSelectionQuery(
            TransformationProjectRepository projectRepository
    ) {
        this.projectRepository =
                Objects.requireNonNull(
                        projectRepository,
                        "TransformationProjectRepository es obligatorio"
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransformationProjectOptionResponse> findAvailableProjects(
            Long storeId
    ) {
        requirePositive(
                storeId,
                "storeId"
        );

        List<TransformationProject> projects =
                Objects.requireNonNull(
                        projectRepository
                                .findAllByStoreIdOrderByCreatedAtDesc(
                                        storeId
                                ),
                        "TransformationProjectRepository devolvió una lista nula"
                );

        return projects.stream()
                .map(
                        DefaultTransformationProjectSelectionQuery::toResponse
                )
                .toList();
    }

    private static TransformationProjectOptionResponse toResponse(
            TransformationProject project
    ) {
        Objects.requireNonNull(
                project,
                "El proyecto de transformación es obligatorio"
        );

        return new TransformationProjectOptionResponse(
                project.getId(),
                project.getCode(),
                project.getName(),
                project.getClientName(),
                project.getProjectType(),
                project.getStatus(),
                project.isSourceOfTruthLocked(),
                project.getCurrentBlueprintVersion(),
                project.getCreatedAt(),
                project.getUpdatedAt()
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
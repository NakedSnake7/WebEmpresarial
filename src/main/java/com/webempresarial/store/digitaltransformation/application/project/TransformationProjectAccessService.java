package com.webempresarial.store.digitaltransformation.application.project;

import com.webempresarial.store.digitaltransformation.application.shared.TransformationProjectNotFoundException;
import com.webempresarial.store.digitaltransformation.application.shared.TransformationSourceNotFoundException;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectRepository;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransformationProjectAccessService {

    private final TransformationProjectRepository projectRepository;
    private final TransformationSourceDocumentRepository sourceRepository;

    public TransformationProjectAccessService(
            TransformationProjectRepository projectRepository,
            TransformationSourceDocumentRepository sourceRepository
    ) {
        this.projectRepository = projectRepository;
        this.sourceRepository = sourceRepository;
    }

    public TransformationProject requireProject(
            Long storeId,
            Long projectId
    ) {
        validateStoreId(storeId);
        validateProjectId(projectId);

        return projectRepository
                .findByIdAndStoreId(projectId, storeId)
                .orElseThrow(() ->
                        new TransformationProjectNotFoundException(
                                projectId,
                                storeId
                        )
                );
    }

    public TransformationSourceDocument requireSource(
            Long storeId,
            Long sourceId
    ) {
        validateStoreId(storeId);
        validateSourceId(sourceId);

        return sourceRepository
                .findByIdAndProjectStoreId(sourceId, storeId)
                .orElseThrow(() ->
                        new TransformationSourceNotFoundException(
                                sourceId,
                                storeId
                        )
                );
    }

    private static void validateStoreId(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }
    }

    private static void validateProjectId(Long projectId) {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException(
                    "El projectId debe ser válido"
            );
        }
    }

    private static void validateSourceId(Long sourceId) {
        if (sourceId == null || sourceId <= 0) {
            throw new IllegalArgumentException(
                    "El sourceId debe ser válido"
            );
        }
    }
}
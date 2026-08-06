package com.webempresarial.store.digitaltransformation.application.project;

import com.webempresarial.store.digitaltransformation.application.shared.DuplicateTransformationProjectException;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectRepository;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class CreateTransformationProjectService {

    private final StoreRepository storeRepository;
    private final TransformationProjectRepository projectRepository;

    public CreateTransformationProjectService(
            StoreRepository storeRepository,
            TransformationProjectRepository projectRepository
    ) {
        this.storeRepository = storeRepository;
        this.projectRepository = projectRepository;
    }

    public TransformationProjectResult create(
            CreateTransformationProjectCommand command
    ) {
        Objects.requireNonNull(
                command,
                "El comando de creación es obligatorio"
        );

        Long storeId = requireValidId(
                command.storeId(),
                "El storeId debe ser válido"
        );

        String code = normalizeProjectCode(command.code());

        if (projectRepository.existsByStoreIdAndCodeIgnoreCase(
                storeId,
                code
        )) {
            throw new DuplicateTransformationProjectException(
                    storeId,
                    code
            );
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe un store con id " + storeId
                        )
                );

        TransformationProject project =
                TransformationProject.create(
                        store,
                        code,
                        command.name(),
                        command.clientName(),
                        command.clientWebsite(),
                        command.projectType(),
                        command.executiveIntent()
                );

        project.markSourcesPending();

        TransformationProject saved =
                projectRepository.save(project);

        return TransformationProjectResult.from(saved);
    }

    private static Long requireValidId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }

        return id;
    }

    private static String normalizeProjectCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del proyecto es obligatorio"
            );
        }

        String normalized = code
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!normalized.matches("^[A-Z0-9][A-Z0-9_-]{2,49}$")) {
            throw new IllegalArgumentException(
                    "El código del proyecto debe contener entre 3 y 50 " +
                    "caracteres alfanuméricos, guiones o guiones bajos"
            );
        }

        return normalized;
    }
}
package com.webempresarial.store.digitaltransformation.application.source;

import com.webempresarial.store.digitaltransformation.application.project.TransformationProjectAccessService;
import com.webempresarial.store.digitaltransformation.application.shared.DuplicateTransformationSourceException;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class RegisterTransformationSourceService {

    private final TransformationProjectAccessService accessService;
    private final TransformationSourceDocumentRepository sourceRepository;

    public RegisterTransformationSourceService(
            TransformationProjectAccessService accessService,
            TransformationSourceDocumentRepository sourceRepository
    ) {
        this.accessService = accessService;
        this.sourceRepository = sourceRepository;
    }

    public TransformationSourceResult register(
            RegisterTransformationSourceCommand command
    ) {
        Objects.requireNonNull(
                command,
                "El comando de registro es obligatorio"
        );

        TransformationProject project =
                accessService.requireProject(
                        command.storeId(),
                        command.projectId()
                );

        project.ensureSourcesCanBeModified();

        String checksum = normalizeChecksum(
                command.checksumSha256()
        );

        if (sourceRepository.existsByProjectIdAndChecksumSha256(
                project.getId(),
                checksum
        )) {
            throw new DuplicateTransformationSourceException(
                    "El documento ya fue registrado en el proyecto"
            );
        }

        if (sourceRepository
                .existsByProjectIdAndSourceTypeAndDocumentVersion(
                        project.getId(),
                        command.sourceType(),
                        command.documentVersion()
                )) {
            throw new DuplicateTransformationSourceException(
                    "Ya existe un documento de tipo " +
                    command.sourceType() +
                    " con versión " +
                    command.documentVersion()
            );
        }

        TransformationSourceDocument document =
                TransformationSourceDocument.register(
                        project,
                        command.sourceType(),
                        command.sourceRole(),
                        command.originalFilename(),
                        command.displayName(),
                        command.mimeType(),
                        command.storageReference(),
                        checksum,
                        command.documentVersion(),
                        normalizeLanguageCode(command.languageCode()),
                        command.pageCount()
                );

        document.markUploaded();

        TransformationSourceDocument saved =
                sourceRepository.save(document);

        return TransformationSourceResult.from(saved);
    }

    private static String normalizeChecksum(String checksum) {
        if (checksum == null) {
            return null;
        }

        return checksum
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private static String normalizeLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            throw new IllegalArgumentException(
                    "El idioma del documento es obligatorio"
            );
        }

        String normalized = languageCode
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!normalized.matches("^[a-z]{2,3}(-[a-z0-9]{2,8})*$")) {
            throw new IllegalArgumentException(
                    "El código de idioma no tiene un formato válido"
            );
        }

        return normalized;
    }
}
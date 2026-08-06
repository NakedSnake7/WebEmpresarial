package com.webempresarial.store.digitaltransformation.application.evidence;

import com.webempresarial.store.digitaltransformation.application.project.TransformationProjectAccessService;
import com.webempresarial.store.digitaltransformation.application.shared.DuplicateSourceEvidenceException;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidenceRepository;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.source.SourceDocumentSection;
import com.webempresarial.store.digitaltransformation.domain.source.SourceDocumentSectionRepository;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class ExtractSourceEvidenceService {

    private final TransformationProjectAccessService accessService;
    private final SourceDocumentSectionRepository sectionRepository;
    private final SourceEvidenceRepository evidenceRepository;

    public ExtractSourceEvidenceService(
            TransformationProjectAccessService accessService,
            SourceDocumentSectionRepository sectionRepository,
            SourceEvidenceRepository evidenceRepository
    ) {
        this.accessService = accessService;
        this.sectionRepository = sectionRepository;
        this.evidenceRepository = evidenceRepository;
    }

    public SourceEvidenceResult extract(
            ExtractSourceEvidenceCommand command
    ) {
        Objects.requireNonNull(
                command,
                "El comando de extracción es obligatorio"
        );

        TransformationProject project =
                accessService.requireProject(
                        command.storeId(),
                        command.projectId()
                );

        TransformationSourceDocument source =
                accessService.requireSource(
                        command.storeId(),
                        command.sourceDocumentId()
                );

        if (!source.getProject().getId()
                .equals(project.getId())) {
            throw new IllegalArgumentException(
                    "El documento no pertenece al proyecto indicado"
            );
        }

        String evidenceCode =
                normalizeCode(command.evidenceCode());

        if (evidenceRepository
                .existsByProjectIdAndEvidenceCodeIgnoreCase(
                        project.getId(),
                        evidenceCode
                )) {
            throw new DuplicateSourceEvidenceException(
                    project.getId(),
                    evidenceCode
            );
        }

        SourceDocumentSection section =
                resolveSection(
                        command.storeId(),
                        command.sourceSectionId()
                );

        SourceEvidence evidence =
                SourceEvidence.extract(
                        project,
                        source,
                        section,
                        evidenceCode,
                        command.classification(),
                        command.confidence(),
                        command.extractionOrigin(),
                        command.statement(),
                        command.supportingExcerpt(),
                        command.interpretation(),
                        command.locator()
                );

        SourceEvidence saved =
                evidenceRepository.save(evidence);

        return SourceEvidenceResult.from(saved);
    }

    private SourceDocumentSection resolveSection(
            Long storeId,
            Long sectionId
    ) {
        if (sectionId == null) {
            return null;
        }

        if (sectionId <= 0) {
            throw new IllegalArgumentException(
                    "El sectionId debe ser válido"
            );
        }

        return sectionRepository
                .findByIdAndSourceContentSourceDocumentProjectStoreId(
                        sectionId,
                        storeId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró la sección " +
                                sectionId +
                                " para el store " +
                                storeId
                        )
                );
    }

    private static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de evidencia es obligatorio"
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }
}
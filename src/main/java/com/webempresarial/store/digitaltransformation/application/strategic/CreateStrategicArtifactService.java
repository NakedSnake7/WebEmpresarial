package com.webempresarial.store.digitaltransformation.application.strategic;

import com.webempresarial.store.digitaltransformation.application.project.TransformationProjectAccessService;
import com.webempresarial.store.digitaltransformation.application.shared.DuplicateStrategicArtifactException;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CreateStrategicArtifactService {

    private final TransformationProjectAccessService projectAccessService;
    private final StrategicArtifactRepository artifactRepository;
    private final StrategicArtifactCodeGenerator codeGenerator;

    public CreateStrategicArtifactService(
            TransformationProjectAccessService projectAccessService,
            StrategicArtifactRepository artifactRepository,
            StrategicArtifactCodeGenerator codeGenerator
    ) {
        this.projectAccessService =
                projectAccessService;

        this.artifactRepository =
                artifactRepository;

        this.codeGenerator =
                codeGenerator;
    }

    public StrategicArtifactResult create(
            CreateStrategicArtifactCommand command
    ) {
        Objects.requireNonNull(
                command,
                "El comando es obligatorio"
        );

        TransformationProject project =
                projectAccessService.requireProject(
                        command.storeId(),
                        command.projectId()
                );

        StrategicArtifactType type =
                Objects.requireNonNull(
                        command.artifactType(),
                        "El tipo estratégico es obligatorio"
                );

        long nextSequence =
                artifactRepository
                        .countByProjectIdAndArtifactType(
                                project.getId(),
                                type
                        ) + 1;

        String code =
                codeGenerator.generate(
                        StrategicArtifactTypeDescriptor.of(
                                type
                        ),
                        nextSequence
                );

        if (artifactRepository
                .existsByProjectIdAndArtifactCodeIgnoreCase(
                        project.getId(),
                        code
                )) {
            throw new DuplicateStrategicArtifactException(
                    project.getId(),
                    code
            );
        }

        StrategicArtifact artifact =
                StrategicArtifact.create(
                        project,
                        code,
                        type,
                        command.confidence(),
                        command.origin(),
                        command.statement(),
                        command.rationale(),
                        command.businessImplication()
                );

        StrategicArtifact saved =
                artifactRepository.save(
                        artifact
                );

        return StrategicArtifactResult.from(
                saved
        );
    }
}
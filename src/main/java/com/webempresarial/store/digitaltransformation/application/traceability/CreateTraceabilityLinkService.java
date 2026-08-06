package com.webempresarial.store.digitaltransformation.application.traceability;

import com.webempresarial.store.digitaltransformation.application.project.TransformationProjectAccessService;
import com.webempresarial.store.digitaltransformation.application.shared.DuplicateTraceabilityLinkException;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CreateTraceabilityLinkService {

    private final TransformationProjectAccessService projectAccessService;
    private final TraceabilityAccessService traceabilityAccessService;
    private final TraceabilityLinkRepository linkRepository;
    private final ProvenanceRecordRepository provenanceRepository;

    public CreateTraceabilityLinkService(
            TransformationProjectAccessService projectAccessService,
            TraceabilityAccessService traceabilityAccessService,
            TraceabilityLinkRepository linkRepository,
            ProvenanceRecordRepository provenanceRepository
    ) {
        this.projectAccessService = projectAccessService;
        this.traceabilityAccessService = traceabilityAccessService;
        this.linkRepository = linkRepository;
        this.provenanceRepository = provenanceRepository;
    }

    public TraceabilityLinkResult create(
            CreateTraceabilityLinkCommand command
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

        TraceabilityNode source =
                traceabilityAccessService.requireNode(
                        command.storeId(),
                        command.sourceNodeId()
                );

        TraceabilityNode target =
                traceabilityAccessService.requireNode(
                        command.storeId(),
                        command.targetNodeId()
                );

        source.ensureBelongsToProject(project);
        target.ensureBelongsToProject(project);

        if (linkRepository
                .existsByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                        project.getId(),
                        source.getId(),
                        target.getId(),
                        command.relationType()
                )) {
            throw new DuplicateTraceabilityLinkException(
                    source.getId(),
                    target.getId()
            );
        }

        TraceabilityLink link =
                TraceabilityLink.create(
                        project,
                        source,
                        target,
                        command.relationType(),
                        command.strength(),
                        command.origin(),
                        command.rationale()
                );

        TraceabilityLink saved =
                linkRepository.save(link);

        provenanceRepository.save(
                ProvenanceRecord.forLink(
                        project,
                        saved,
                        ProvenanceAction.CREATED,
                        command.origin(),
                        command.actor(),
                        resolveActorType(command.origin()),
                        "CreateTraceabilityLinkService",
                        "Creación de relación " +
                        command.relationType() +
                        " entre " +
                        source.getNodeCode() +
                        " y " +
                        target.getNodeCode()
                )
        );

        return TraceabilityLinkResult.from(saved);
    }

    private static String resolveActorType(
            TraceabilityOrigin origin
    ) {
        return switch (origin) {
            case AI_ASSISTED -> "AI_AGENT";
            case SYSTEM_GENERATED, RULE_BASED -> "SYSTEM";
            case DOCUMENT_EXTRACTION -> "EXTRACTION_ENGINE";
            case IMPORTED -> "IMPORT_PROCESS";
            case MANUAL -> "USER";
        };
    }
}
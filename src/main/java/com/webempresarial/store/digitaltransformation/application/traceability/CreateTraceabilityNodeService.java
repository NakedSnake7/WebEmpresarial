package com.webempresarial.store.digitaltransformation.application.traceability;

import com.webempresarial.store.digitaltransformation.application.project.TransformationProjectAccessService;
import com.webempresarial.store.digitaltransformation.application.shared.DuplicateTraceabilityNodeException;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class CreateTraceabilityNodeService {

    private final TransformationProjectAccessService accessService;
    private final TraceabilityNodeRepository nodeRepository;
    private final ProvenanceRecordRepository provenanceRepository;

    public CreateTraceabilityNodeService(
            TransformationProjectAccessService accessService,
            TraceabilityNodeRepository nodeRepository,
            ProvenanceRecordRepository provenanceRepository
    ) {
        this.accessService = accessService;
        this.nodeRepository = nodeRepository;
        this.provenanceRepository = provenanceRepository;
    }

    public TraceabilityNodeResult create(
            CreateTraceabilityNodeCommand command
    ) {
        Objects.requireNonNull(
                command,
                "El comando es obligatorio"
        );

        TransformationProject project =
                accessService.requireProject(
                        command.storeId(),
                        command.projectId()
                );

        String normalizedCode =
                normalizeCode(command.nodeCode());

        if (nodeRepository
                .existsByProjectIdAndNodeCodeIgnoreCase(
                        project.getId(),
                        normalizedCode
                )) {
            throw new DuplicateTraceabilityNodeException(
                    project.getId(),
                    normalizedCode
            );
        }

        if (nodeRepository
                .existsByProjectIdAndNodeTypeAndExternalReference(
                        project.getId(),
                        command.nodeType(),
                        command.externalReference()
                )) {
            throw new DuplicateTraceabilityNodeException(
                    project.getId(),
                    normalizedCode
            );
        }

        TraceabilityNode node =
                TraceabilityNode.create(
                        project,
                        normalizedCode,
                        command.nodeType(),
                        command.origin(),
                        command.title(),
                        command.description(),
                        command.externalReference(),
                        command.externalEntityType(),
                        command.requiresReview()
                );

        TraceabilityNode saved =
                nodeRepository.save(node);

        ProvenanceRecord provenance =
                ProvenanceRecord.forNode(
                        project,
                        saved,
                        ProvenanceAction.CREATED,
                        command.origin(),
                        command.actor(),
                        resolveActorType(command.origin()),
                        "CreateTraceabilityNodeService",
                        "Creación del nodo de trazabilidad " +
                        normalizedCode
                );

        provenanceRepository.save(provenance);

        return TraceabilityNodeResult.from(saved);
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

    private static String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del nodo es obligatorio"
            );
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
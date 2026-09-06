package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactRepository;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class CreateStrategicRelationshipService {

    private final StrategicArtifactRepository artifactRepository;
    private final StrategicRelationshipRepository relationshipRepository;
    private final StrategicRelationshipPolicy relationshipPolicy;
    private final StrategicCycleDetector cycleDetector;
    private final StrategicRelationshipTraceabilitySynchronizer traceabilitySynchronizer;
    private final StrategicRelationshipProvenanceRecorder provenanceRecorder;

    public CreateStrategicRelationshipService(
            StrategicArtifactRepository artifactRepository,
            StrategicRelationshipRepository relationshipRepository,
            StrategicRelationshipPolicy relationshipPolicy,
            StrategicCycleDetector cycleDetector,
            StrategicRelationshipTraceabilitySynchronizer traceabilitySynchronizer,
            StrategicRelationshipProvenanceRecorder provenanceRecorder
    ) {
        this.artifactRepository =
                Objects.requireNonNull(
                        artifactRepository
                );

        this.relationshipRepository =
                Objects.requireNonNull(
                        relationshipRepository
                );

        this.relationshipPolicy =
                Objects.requireNonNull(
                        relationshipPolicy
                );

        this.cycleDetector =
                Objects.requireNonNull(
                        cycleDetector
                );

        this.traceabilitySynchronizer =
                Objects.requireNonNull(
                        traceabilitySynchronizer
                );

        this.provenanceRecorder =
                Objects.requireNonNull(
                        provenanceRecorder
                );
    }

    public CreateStrategicRelationshipResult create(
            CreateStrategicRelationshipCommand command
    ) {
        Objects.requireNonNull(
                command,
                "El comando es obligatorio"
        );

        StrategicArtifact source =
                findArtifact(
                        command.sourceArtifactId(),
                        command.projectId(),
                        command.storeId(),
                        "origen"
                );

        StrategicArtifact target =
                findArtifact(
                        command.targetArtifactId(),
                        command.projectId(),
                        command.storeId(),
                        "destino"
                );

        relationshipPolicy.validate(
                source,
                target,
                command.relationshipType()
        );

        Optional<StrategicRelationship> existing =
                relationshipRepository
                        .findByProjectIdAndSourceArtifactIdAndTargetArtifactIdAndRelationshipType(
                                command.projectId(),
                                source.getId(),
                                target.getId(),
                                command.relationshipType()
                        );

        if (existing.isPresent()) {
            StrategicRelationship existingRelationship =
                    existing.get();

            StrategicRelationshipTraceabilitySync traceabilitySync =
                    traceabilitySynchronizer.synchronize(
                            existingRelationship
                    );

            if (traceabilitySync.created()) {
                provenanceRecorder.record(
                        existingRelationship,
                        traceabilitySync.link()
                );
            }

            return result(
                    existingRelationship,
                    false
            );
        }

        if (cycleDetector.wouldCreateCycle(
                command.projectId(),
                source.getId(),
                target.getId()
        )) {
            throw new IllegalStateException(
                    "La relación produciría un ciclo en el grafo estratégico"
            );
        }

        StrategicRelationship relationship =
                StrategicRelationship.create(
                        source.getProject(),
                        source,
                        target,
                        command.relationshipType(),
                        command.origin(),
                        command.rationale()
                );

        StrategicRelationship saved =
                relationshipRepository.save(
                        relationship
                );

        StrategicRelationshipTraceabilitySync traceabilitySync =
                traceabilitySynchronizer.synchronize(
                        saved
                );

        if (traceabilitySync.created()) {
            provenanceRecorder.record(
                    saved,
                    traceabilitySync.link()
            );
        }

        return result(
                saved,
                true
        );
    }

    private StrategicArtifact findArtifact(
            Long artifactId,
            Long projectId,
            Long storeId,
            String role
    ) {
        return artifactRepository
                .findByIdAndProjectIdAndProjectStoreId(
                        artifactId,
                        projectId,
                        storeId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el artefacto estratégico " +
                                role +
                                " " +
                                artifactId +
                                " para el proyecto " +
                                projectId
                        )
                );
    }

    private static CreateStrategicRelationshipResult result(
            StrategicRelationship relationship,
            boolean created
    ) {
        return new CreateStrategicRelationshipResult(
                relationship.getId(),
                relationship.getSourceArtifact().getId(),
                relationship.getSourceArtifact().getArtifactCode(),
                relationship.getTargetArtifact().getId(),
                relationship.getTargetArtifact().getArtifactCode(),
                relationship.getRelationshipType(),
                relationship.getStatus(),
                created
        );
    }
}
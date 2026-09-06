package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactRepository;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationship;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipRepository;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipType;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChain;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class DefaultStrategicChainResolver
        implements StrategicChainResolver {

    private final StrategicArtifactRepository
            artifactRepository;

    private final StrategicRelationshipRepository
            relationshipRepository;

    public DefaultStrategicChainResolver(
            StrategicArtifactRepository artifactRepository,
            StrategicRelationshipRepository relationshipRepository
    ) {
        this.artifactRepository =
                Objects.requireNonNull(
                        artifactRepository,
                        "StrategicArtifactRepository es obligatorio"
                );

        this.relationshipRepository =
                Objects.requireNonNull(
                        relationshipRepository,
                        "StrategicRelationshipRepository es obligatorio"
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StrategicChain> resolve(
            Long projectId
    ) {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException(
                    "El projectId debe ser válido"
            );
        }

        List<StrategicArtifact> findings =
                artifactRepository
                        .findAllByProjectIdAndArtifactTypeOrderByCreatedAtAsc(
                                projectId,
                                StrategicArtifactType.FINDING
                        );

        for (StrategicArtifact finding : findings) {

            Optional<StrategicArtifact> problem =
                    findTarget(
                            projectId,
                            finding,
                            StrategicRelationshipType.REVEALS,
                            StrategicArtifactType.BUSINESS_PROBLEM
                    );

            if (problem.isEmpty()) {
                continue;
            }

            Optional<StrategicArtifact> objective =
                    findTarget(
                            projectId,
                            problem.get(),
                            StrategicRelationshipType.ADDRESSED_BY,
                            StrategicArtifactType.BUSINESS_OBJECTIVE
                    );

            if (objective.isEmpty()) {
                continue;
            }

            Optional<StrategicArtifact> opportunity =
                    findTarget(
                            projectId,
                            objective.get(),
                            StrategicRelationshipType.ENABLES,
                            StrategicArtifactType.STRATEGIC_OPPORTUNITY
                    );

            if (opportunity.isEmpty()) {
                continue;
            }

            return Optional.of(
                    StrategicChain.of(
                            finding.getProject(),
                            finding,
                            problem.get(),
                            objective.get(),
                            opportunity.get()
                    )
            );
        }

        return Optional.empty();
    }

    private Optional<StrategicArtifact> findTarget(
            Long projectId,
            StrategicArtifact source,
            StrategicRelationshipType relationshipType,
            StrategicArtifactType expectedTargetType
    ) {
        if (source.getId() == null) {
            return Optional.empty();
        }

        List<StrategicRelationship> relationships =
                relationshipRepository
                        .findAllByProjectIdAndSourceArtifactIdAndStatus(
                                projectId,
                                source.getId(),
                                StrategicRelationshipStatus.ACTIVE
                        );

        List<StrategicArtifact> candidates =
                relationships.stream()
                        .filter(relationship ->
                                relationship.getRelationshipType()
                                        == relationshipType
                        )
                        .map(
                                StrategicRelationship::getTargetArtifact
                        )
                        .filter(Objects::nonNull)
                        .filter(target ->
                                target.getArtifactType()
                                        == expectedTargetType
                        )
                        .toList();

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        if (candidates.size() > 1) {
            throw new IllegalStateException(
                    "Existe más de una relación estratégica activa de tipo "
                            + relationshipType
                            + " para el artefacto "
                            + source.getArtifactCode()
            );
        }

        return Optional.of(
                candidates.getFirst()
        );
    }
}
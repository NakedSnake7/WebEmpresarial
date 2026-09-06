package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class RepositoryStrategicCycleDetector
        implements StrategicCycleDetector {

    private final StrategicRelationshipRepository repository;

    public RepositoryStrategicCycleDetector(
            StrategicRelationshipRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public boolean wouldCreateCycle(
            Long projectId,
            Long sourceArtifactId,
            Long targetArtifactId
    ) {
        if (sourceArtifactId.equals(targetArtifactId)) {
            return true;
        }

        /*
         * Si desde TARGET ya podemos alcanzar SOURCE,
         * agregar SOURCE -> TARGET cerraría un ciclo.
         */
        return canReach(
                projectId,
                targetArtifactId,
                sourceArtifactId,
                new HashSet<>()
        );
    }

    private boolean canReach(
            Long projectId,
            Long currentArtifactId,
            Long expectedArtifactId,
            Set<Long> visited
    ) {
        if (currentArtifactId.equals(expectedArtifactId)) {
            return true;
        }

        if (!visited.add(currentArtifactId)) {
            return false;
        }

        return repository
                .findAllByProjectIdAndSourceArtifactId(
                        projectId,
                        currentArtifactId
                )
                .stream()
                .filter(StrategicRelationship::isActive)
                .map(StrategicRelationship::getTargetArtifact)
                .anyMatch(target ->
                        canReach(
                                projectId,
                                target.getId(),
                                expectedArtifactId,
                                visited
                        )
                );
    }
}
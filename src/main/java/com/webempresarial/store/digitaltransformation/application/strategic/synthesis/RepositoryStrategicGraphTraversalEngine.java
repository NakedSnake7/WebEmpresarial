package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.*; 
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class RepositoryStrategicGraphTraversalEngine
        implements StrategicGraphTraversalEngine {

    private final StrategicArtifactRepository artifactRepository;
    private final StrategicRelationshipRepository relationshipRepository;

    public RepositoryStrategicGraphTraversalEngine(
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
    
    private static void ensureDistinctArtifacts(
            StrategicArtifact finding,
            StrategicArtifact problem,
            StrategicArtifact objective,
            StrategicArtifact opportunity
    ) {
        List<Long> ids =
                List.of(
                        finding,
                        problem,
                        objective,
                        opportunity
                )
                .stream()
                .map(StrategicArtifact::getId)
                .filter(Objects::nonNull)
                .toList();

        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException(
                    "El grafo estratégico contiene un ciclo o reutilización " +
                    "inválida de artefactos"
            );
        }
    }

    @Override
    public StrategicTraversalResult traverseFromFinding(
            Long storeId,
            Long projectId,
            Long findingArtifactId
    ) {
        validateId(storeId, "El storeId debe ser válido");
        validateId(projectId, "El projectId debe ser válido");
        validateId(
                findingArtifactId,
                "El findingArtifactId debe ser válido"
        );

        StrategicArtifact finding =
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                findingArtifactId,
                                projectId,
                                storeId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró el hallazgo estratégico " +
                                        findingArtifactId +
                                        " para el proyecto " +
                                        projectId
                                )
                        );

        if (finding.getArtifactType()
                != StrategicArtifactType.FINDING) {
            throw new IllegalArgumentException(
                    "El artefacto inicial debe ser de tipo FINDING"
            );
        }

        StepResult problemStep =
                resolveNext(
                        projectId,
                        finding,
                        StrategicRelationshipType.REVEALS,
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        StrategicTraversalAmbiguityType.MULTIPLE_BUSINESS_PROBLEMS
                );

        if (problemStep.ambiguous()) {
            return ambiguous(
                    finding,
                    null,
                    null,
                    null,
                    problemStep.ambiguity()
            );
        }

        StrategicArtifact problem =
                problemStep.artifact();

        if (problem == null) {
            return incomplete(
                    finding,
                    null,
                    null,
                    null
            );
        }

        StepResult objectiveStep =
                resolveNext(
                        projectId,
                        problem,
                        StrategicRelationshipType.ADDRESSED_BY,
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        StrategicTraversalAmbiguityType.MULTIPLE_BUSINESS_OBJECTIVES
                );

        if (objectiveStep.ambiguous()) {
            return ambiguous(
                    finding,
                    problem,
                    null,
                    null,
                    objectiveStep.ambiguity()
            );
        }

        StrategicArtifact objective =
                objectiveStep.artifact();

        if (objective == null) {
            return incomplete(
                    finding,
                    problem,
                    null,
                    null
            );
        }

        StepResult opportunityStep =
                resolveNext(
                        projectId,
                        objective,
                        StrategicRelationshipType.ENABLES,
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        StrategicTraversalAmbiguityType.MULTIPLE_STRATEGIC_OPPORTUNITIES
                );

        if (opportunityStep.ambiguous()) {
            return ambiguous(
                    finding,
                    problem,
                    objective,
                    null,
                    opportunityStep.ambiguity()
            );
        }

        StrategicArtifact opportunity =
                opportunityStep.artifact();

        if (opportunity == null) {
            return incomplete(
                    finding,
                    problem,
                    objective,
                    null
            );
        }
        ensureDistinctArtifacts(
                finding,
                problem,
                objective,
                opportunity
        );

        return StrategicTraversalResult.of(
                StrategicTraversalStatus.COMPLETE,
                finding,
                problem,
                objective,
                opportunity,
                List.of(),
                List.of()
        );
    }

    private StepResult resolveNext(
            Long projectId,
            StrategicArtifact source,
            StrategicRelationshipType expectedRelationshipType,
            StrategicArtifactType expectedTargetType,
            StrategicTraversalAmbiguityType ambiguityType
    ) {
        List<StrategicRelationship> relationships =
                relationshipRepository
                        .findAllByProjectIdAndSourceArtifactIdAndStatus(
                                projectId,
                                source.getId(),
                                StrategicRelationshipStatus.ACTIVE
                        )
                        .stream()
                        .filter(relationship ->
                                relationship.getRelationshipType()
                                == expectedRelationshipType
                        )
                        .toList();

        if (relationships.isEmpty()) {
            return StepResult.missing();
        }

        List<StrategicArtifact> candidates =
                relationships.stream()
                        .map(
                                StrategicRelationship::getTargetArtifact
                        )
                        .filter(Objects::nonNull)
                        .toList();

        boolean invalidTarget =
                candidates.stream()
                        .anyMatch(candidate ->
                                candidate.getArtifactType()
                                != expectedTargetType
                        );

        if (invalidTarget) {
            throw new IllegalStateException(
                    "El grafo contiene una relación " +
                    expectedRelationshipType +
                    " hacia un tipo estratégico inválido"
            );
        }

        if (candidates.size() > 1) {
            return StepResult.ambiguous(
                    new StrategicTraversalAmbiguity(
                            ambiguityType,
                            source.getArtifactCode(),
                            candidates.stream()
                                    .map(
                                            StrategicArtifact::getArtifactCode
                                    )
                                    .sorted()
                                    .toList(),
                            "Se encontraron múltiples destinos válidos " +
                            "para la relación " +
                            expectedRelationshipType
                    )
            );
        }

        return StepResult.resolved(
                candidates.getFirst()
        );
    }

    private static StrategicTraversalResult incomplete(
            StrategicArtifact finding,
            StrategicArtifact problem,
            StrategicArtifact objective,
            StrategicArtifact opportunity
    ) {
        StrategicChain chain =
                StrategicChain.of(
                        finding.getProject(),
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        return StrategicTraversalResult.of(
                StrategicTraversalStatus.INCOMPLETE,
                finding,
                problem,
                objective,
                opportunity,
                chain.getGaps(),
                List.of()
        );
    }

    private static StrategicTraversalResult ambiguous(
            StrategicArtifact finding,
            StrategicArtifact problem,
            StrategicArtifact objective,
            StrategicArtifact opportunity,
            StrategicTraversalAmbiguity ambiguity
    ) {
        return StrategicTraversalResult.of(
                StrategicTraversalStatus.AMBIGUOUS,
                finding,
                problem,
                objective,
                opportunity,
                List.of(),
                List.of(ambiguity)
        );
    }

    private static void validateId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private record StepResult(
            StrategicArtifact artifact,
            StrategicTraversalAmbiguity ambiguity
    ) {

        static StepResult resolved(
                StrategicArtifact artifact
        ) {
            return new StepResult(
                    Objects.requireNonNull(artifact),
                    null
            );
        }

        static StepResult missing() {
            return new StepResult(
                    null,
                    null
            );
        }

        static StepResult ambiguous(
                StrategicTraversalAmbiguity ambiguity
        ) {
            return new StepResult(
                    null,
                    Objects.requireNonNull(ambiguity)
            );
        }

        boolean ambiguous() {
            return ambiguity != null;
        }
    }
    
    
}
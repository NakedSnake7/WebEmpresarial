package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationship;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class StrategicRelationshipTraceabilitySynchronizer {

    private final StrategicRelationshipTraceabilityMapper mapper;
    private final StrategicArtifactTraceabilityNodeResolver nodeResolver;
    private final TraceabilityLinkRepository linkRepository;

    public StrategicRelationshipTraceabilitySynchronizer(
            StrategicRelationshipTraceabilityMapper mapper,
            StrategicArtifactTraceabilityNodeResolver nodeResolver,
            TraceabilityLinkRepository linkRepository
    ) {
        this.mapper =
                Objects.requireNonNull(
                        mapper,
                        "StrategicRelationshipTraceabilityMapper es obligatorio"
                );

        this.nodeResolver =
                Objects.requireNonNull(
                        nodeResolver,
                        "StrategicArtifactTraceabilityNodeResolver es obligatorio"
                );

        this.linkRepository =
                Objects.requireNonNull(
                        linkRepository,
                        "TraceabilityLinkRepository es obligatorio"
                );
    }

    public StrategicRelationshipTraceabilitySync synchronize(
            StrategicRelationship relationship
    ) {
        Objects.requireNonNull(
                relationship,
                "La relación estratégica es obligatoria"
        );

        if (!relationship.isActive()) {
            throw new IllegalStateException(
                    "Solo una relación estratégica ACTIVE puede " +
                    "sincronizarse con trazabilidad"
            );
        }

        StrategicRelationshipTraceabilityMapping mapping =
                mapper.map(
                        relationship.getRelationshipType()
                );

        StrategicArtifact semanticSource =
                relationship.getSourceArtifact();

        StrategicArtifact semanticTarget =
                relationship.getTargetArtifact();

        TraceabilityNode sourceNode;
        TraceabilityNode targetNode;

        if (mapping.reverseDirection()) {
            sourceNode =
                    nodeResolver.requireNode(
                            semanticTarget
                    );

            targetNode =
                    nodeResolver.requireNode(
                            semanticSource
                    );

        } else {
            sourceNode =
                    nodeResolver.requireNode(
                            semanticSource
                    );

            targetNode =
                    nodeResolver.requireNode(
                            semanticTarget
                    );
        }

        Optional<TraceabilityLink> existing =
                linkRepository
                        .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                                relationship.getProject().getId(),
                                sourceNode.getId(),
                                targetNode.getId(),
                                mapping.relationType()
                        );

	        if (existing.isPresent()) {
	            return new StrategicRelationshipTraceabilitySync(
	                    existing.get(),
	                    false
	            );
	        }

        TraceabilityLink link =
                TraceabilityLink.create(
                        relationship.getProject(),
                        sourceNode,
                        targetNode,
                        mapping.relationType(),
                        mapping.strength(),
                        resolveOrigin(
                                relationship
                        ),
                        buildRationale(
                                relationship
                        )
                );

        TraceabilityLink saved =
                linkRepository.save(link);

        return new StrategicRelationshipTraceabilitySync(
                saved,
                true
        );
    }

    private static TraceabilityOrigin resolveOrigin(
            StrategicRelationship relationship
    ) {
        return switch (relationship.getOrigin()) {

            case MANUAL ->
                    TraceabilityOrigin.MANUAL;

            case RULE_ENGINE ->
                    TraceabilityOrigin.RULE_BASED;

            case INFERENCE_ENGINE ->
                    TraceabilityOrigin.AI_ASSISTED;
        };
    }

    private static String buildRationale(
            StrategicRelationship relationship
    ) {
        StringBuilder result =
                new StringBuilder();

        result.append(
                "Relación estratégica "
        );

        result.append(
                relationship.getRelationshipType()
        );

        result.append(
                ": "
        );

        result.append(
                relationship
                        .getSourceArtifact()
                        .getArtifactCode()
        );

        result.append(
                " -> "
        );

        result.append(
                relationship
                        .getTargetArtifact()
                        .getArtifactCode()
        );

        if (relationship.getRationale() != null) {
            result.append(
                    ". "
            );

            result.append(
                    relationship.getRationale()
            );
        }

        return result.toString();
    }


}
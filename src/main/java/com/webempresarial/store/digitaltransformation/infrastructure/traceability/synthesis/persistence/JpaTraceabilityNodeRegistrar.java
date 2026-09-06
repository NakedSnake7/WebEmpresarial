package com.webempresarial.store.digitaltransformation.infrastructure.traceability.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.shared.DuplicateTraceabilityNodeException;
import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityNodeRegistrar;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JpaTraceabilityNodeRegistrar
        implements TraceabilityNodeRegistrar {

    private final TraceabilityNodeRepository nodeRepository;

    public JpaTraceabilityNodeRegistrar(
            TraceabilityNodeRepository nodeRepository
    ) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public TraceabilityNode register(
            TransformationProject project,
            String nodeCode,
            TraceabilityNodeType nodeType,
            TraceabilityOrigin origin,
            String title,
            String description,
            String externalReference,
            String externalEntityType,
            boolean requiresReview
    ) {
        Objects.requireNonNull(
                project,
                "El proyecto es obligatorio"
        );

        if (nodeRepository
                .existsByProjectIdAndNodeCodeIgnoreCase(
                        project.getId(),
                        nodeCode
                )) {
            throw new DuplicateTraceabilityNodeException(
                    project.getId(),
                    nodeCode
            );
        }

        TraceabilityNode node =
                TraceabilityNode.create(
                        project,
                        nodeCode,
                        nodeType,
                        origin,
                        title,
                        description,
                        externalReference,
                        externalEntityType,
                        requiresReview
                );

        return nodeRepository.save(node);
    }
}
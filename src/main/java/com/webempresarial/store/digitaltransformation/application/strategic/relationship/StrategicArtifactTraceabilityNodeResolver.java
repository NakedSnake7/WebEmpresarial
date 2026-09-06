package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.application.strategic.StrategicTraceabilityTypeMapper;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StrategicArtifactTraceabilityNodeResolver {

    private final TraceabilityNodeRepository nodeRepository;
    private final StrategicTraceabilityTypeMapper typeMapper;

    public StrategicArtifactTraceabilityNodeResolver(
            TraceabilityNodeRepository nodeRepository,
            StrategicTraceabilityTypeMapper typeMapper
    ) {
        this.nodeRepository =
                Objects.requireNonNull(
                        nodeRepository,
                        "TraceabilityNodeRepository es obligatorio"
                );

        this.typeMapper =
                Objects.requireNonNull(
                        typeMapper,
                        "StrategicTraceabilityTypeMapper es obligatorio"
                );
    }

    public TraceabilityNode requireNode(
            StrategicArtifact artifact
    ) {
        Objects.requireNonNull(
                artifact,
                "El artefacto estratégico es obligatorio"
        );

        TraceabilityNodeType nodeType =
                typeMapper.map(
                        artifact.getArtifactType()
                );

        return nodeRepository
                .findByProjectIdAndNodeTypeAndExternalReference(
                        artifact.getProject().getId(),
                        nodeType,
                        artifact.getArtifactCode()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe nodo de trazabilidad para el " +
                                "artefacto estratégico " +
                                artifact.getArtifactCode()
                        )
                );
    }
}
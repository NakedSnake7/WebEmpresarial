package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.StrategicTraceabilityTypeMapper;
import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityNodeRegistrar;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityOrigin;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StrategicTraceabilityRegistrar {

    private final TraceabilityNodeRepository nodeRepository;
    private final TraceabilityNodeRegistrar nodeRegistrar;
    private final StrategicTraceabilityTypeMapper typeMapper;

    public StrategicTraceabilityRegistrar(
            TraceabilityNodeRepository nodeRepository,
            TraceabilityNodeRegistrar nodeRegistrar,
            StrategicTraceabilityTypeMapper typeMapper
    ) {
        this.nodeRepository =
                Objects.requireNonNull(
                        nodeRepository,
                        "TraceabilityNodeRepository es obligatorio"
                );

        this.nodeRegistrar =
                Objects.requireNonNull(
                        nodeRegistrar,
                        "TraceabilityNodeRegistrar es obligatorio"
                );

        this.typeMapper =
                Objects.requireNonNull(
                        typeMapper,
                        "StrategicTraceabilityTypeMapper es obligatorio"
                );
    }

    public TraceabilityNode register(
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
                .orElseGet(() ->
                        nodeRegistrar.register(
                                artifact.getProject(),
                                artifact.getArtifactCode(),
                                nodeType,
                                TraceabilityOrigin.SYSTEM_GENERATED,
                                artifact.getStatement(),
                                artifact.getRationale(),
                                artifact.getArtifactCode(),
                                StrategicArtifact.class.getSimpleName(),
                                artifact.isRequiresReview()
                        )
                );
    }
}
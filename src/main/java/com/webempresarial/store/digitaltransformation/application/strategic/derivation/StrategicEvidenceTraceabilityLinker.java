package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityLink;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityLinkRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityOrigin;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityRelationType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StrategicEvidenceTraceabilityLinker {

    private final TraceabilityNodeRepository nodeRepository;
    private final TraceabilityLinkRepository linkRepository;

    public StrategicEvidenceTraceabilityLinker(
            TraceabilityNodeRepository nodeRepository,
            TraceabilityLinkRepository linkRepository
    ) {
        this.nodeRepository =
                Objects.requireNonNull(
                        nodeRepository,
                        "TraceabilityNodeRepository es obligatorio"
                );

        this.linkRepository =
                Objects.requireNonNull(
                        linkRepository,
                        "TraceabilityLinkRepository es obligatorio"
                );
    }

    public TraceabilityLink link(
            SourceEvidence evidence,
            StrategicArtifact artifact,
            TraceabilityNode strategicNode
    ) {
        Objects.requireNonNull(
                evidence,
                "La evidencia es obligatoria"
        );

        Objects.requireNonNull(
                artifact,
                "El artefacto estratégico es obligatorio"
        );

        Objects.requireNonNull(
                strategicNode,
                "El nodo estratégico es obligatorio"
        );

        evidence.ensureBelongsToProject(
                artifact.getProject()
        );

        strategicNode.ensureBelongsToProject(
                artifact.getProject()
        );

        TraceabilityNode evidenceNode =
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                evidence.getProject().getId(),
                                TraceabilityNodeType.SOURCE_EVIDENCE,
                                evidence.getEvidenceCode()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "La evidencia no posee un nodo de " +
                                        "trazabilidad registrado: " +
                                        evidence.getEvidenceCode()
                                )
                        );

        return linkRepository
                .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                        artifact.getProject().getId(),
                        strategicNode.getId(),
                        evidenceNode.getId(),
                        TraceabilityRelationType.DERIVED_FROM
                )
                .orElseGet(() ->
                        createLink(
                                artifact,
                                strategicNode,
                                evidenceNode
                        )
                );
    }

    private TraceabilityLink createLink(
            StrategicArtifact artifact,
            TraceabilityNode strategicNode,
            TraceabilityNode evidenceNode
    ) {
        TraceabilityLink link =
                TraceabilityLink.create(
                        artifact.getProject(),
                        strategicNode,
                        evidenceNode,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.DIRECT,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        "El artefacto estratégico fue derivado " +
                        "directamente de la evidencia fuente"
                );

        return linkRepository.save(
                link
        );
    }
}
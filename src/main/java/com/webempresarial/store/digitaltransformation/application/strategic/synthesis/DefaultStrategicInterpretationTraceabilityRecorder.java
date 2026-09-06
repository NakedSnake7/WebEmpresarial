package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationAudit;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityLink;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityLinkRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityOrigin;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityRelationType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
public class DefaultStrategicInterpretationTraceabilityRecorder
        implements StrategicInterpretationTraceabilityRecorder {

    private final StrategicSynthesisTraceabilityRegistrar
            synthesisTraceabilityRegistrar;

    private final TraceabilityLinkRepository
            traceabilityLinkRepository;

    public DefaultStrategicInterpretationTraceabilityRecorder(
            StrategicSynthesisTraceabilityRegistrar synthesisTraceabilityRegistrar,
            TraceabilityLinkRepository traceabilityLinkRepository
    ) {
        this.synthesisTraceabilityRegistrar =
                Objects.requireNonNull(
                        synthesisTraceabilityRegistrar,
                        "StrategicSynthesisTraceabilityRegistrar es obligatorio"
                );

        this.traceabilityLinkRepository =
                Objects.requireNonNull(
                        traceabilityLinkRepository,
                        "TraceabilityLinkRepository es obligatorio"
                );
    }

    @Override
    @Transactional
    public void record(
            StrategicSynthesis sourceSynthesis,
            StrategicSynthesis aiSynthesis,
            StrategicInterpretationAudit audit
    ) {
        Objects.requireNonNull(
                sourceSynthesis,
                "La síntesis fuente es obligatoria"
        );

        Objects.requireNonNull(
                aiSynthesis,
                "La síntesis AI es obligatoria"
        );

        Objects.requireNonNull(
                audit,
                "El audit de interpretación es obligatorio"
        );

        ensureValidOrigins(
                sourceSynthesis,
                aiSynthesis
        );

        ensureSameProject(
                sourceSynthesis,
                aiSynthesis
        );

        TraceabilityNode sourceNode =
                synthesisTraceabilityRegistrar.register(
                        aiSynthesis
                );

        TraceabilityNode targetNode =
                synthesisTraceabilityRegistrar.register(
                        sourceSynthesis
                );

        ensurePersistentNodes(
                sourceNode,
                targetNode
        );

        traceabilityLinkRepository
                .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                        aiSynthesis.getProject().getId(),
                        sourceNode.getId(),
                        targetNode.getId(),
                        TraceabilityRelationType.DERIVED_FROM
                )
                .orElseGet(() ->
                        createLink(
                                aiSynthesis,
                                sourceNode,
                                targetNode,
                                audit
                        )
                );
    }

    private TraceabilityLink createLink(
            StrategicSynthesis aiSynthesis,
            TraceabilityNode sourceNode,
            TraceabilityNode targetNode,
            StrategicInterpretationAudit audit
    ) {
        TraceabilityLink link =
                TraceabilityLink.create(
                        aiSynthesis.getProject(),
                        sourceNode,
                        targetNode,
                        TraceabilityRelationType.DERIVED_FROM,
                        TraceabilityStrength.STRONG,
                        TraceabilityOrigin.AI_ASSISTED,
                        buildRationale(
                                audit
                        )
                );

        return traceabilityLinkRepository.save(
                link
        );
    }

    private static void ensureValidOrigins(
            StrategicSynthesis sourceSynthesis,
            StrategicSynthesis aiSynthesis
    ) {
        if (sourceSynthesis.getOrigin()
                != com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisOrigin.DETERMINISTIC) {

            throw new IllegalArgumentException(
                    "La síntesis fuente debe ser DETERMINISTIC"
            );
        }

        if (aiSynthesis.getOrigin()
                != com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisOrigin.AI_ASSISTED) {

            throw new IllegalArgumentException(
                    "La síntesis derivada debe ser AI_ASSISTED"
            );
        }
    }

    private static void ensureSameProject(
            StrategicSynthesis sourceSynthesis,
            StrategicSynthesis aiSynthesis
    ) {
        if (sourceSynthesis.getProject()
                == aiSynthesis.getProject()) {
            return;
        }

        if (sourceSynthesis.getProject().getId() != null
                && aiSynthesis.getProject().getId() != null
                && sourceSynthesis.getProject().getId()
                .equals(
                        aiSynthesis.getProject().getId()
                )) {
            return;
        }

        throw new IllegalArgumentException(
                "Las síntesis deben pertenecer al mismo proyecto"
        );
    }

    private static void ensurePersistentNodes(
            TraceabilityNode sourceNode,
            TraceabilityNode targetNode
    ) {
        if (sourceNode == null || targetNode == null) {
            throw new IllegalStateException(
                    "Los nodos de síntesis son obligatorios"
            );
        }

        if (sourceNode.getId() == null
                || targetNode.getId() == null) {
            throw new IllegalStateException(
                    "Los nodos de síntesis deben estar persistidos antes de crear la relación"
            );
        }
    }

    private static String buildRationale(
            StrategicInterpretationAudit audit
    ) {
        return "AI strategic interpretation derived from deterministic synthesis. " +
                "mode=" +
                audit.getMode() +
                ", validationStatus=" +
                audit.getValidationStatus() +
                ", constraints=" +
                audit.getConstraints() +
                ", sourceArtifacts=" +
                String.join(
                        ",",
                        audit.getSourceArtifactCodes()
                ) +
                ", referencedArtifacts=" +
                String.join(
                        ",",
                        audit.getReferencedArtifactCodes()
                ) +
                ", violations=" +
                audit.getViolations();
    }
}
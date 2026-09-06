package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.application.strategic.relationship.StrategicArtifactTraceabilityNodeResolver;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicArtifactEvidenceSupport;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Transactional(readOnly = true)
public class RepositoryStrategicEvidenceLineageAnalyzer
        implements StrategicEvidenceLineageAnalyzer {

    /*
     * Protección defensiva.
     *
     * La cadena estratégica actual normalmente necesita
     * como máximo 4 saltos:
     *
     * OPP -> OBJ -> PRB -> FND -> EVIDENCE
     *
     * 16 deja margen para futuras extensiones sin permitir
     * recorridos indefinidos sobre datos corruptos.
     */
    private static final int MAX_DEPTH = 16;

    private final StrategicArtifactTraceabilityNodeResolver nodeResolver;
    private final TraceabilityLinkRepository linkRepository;

    public RepositoryStrategicEvidenceLineageAnalyzer(
            StrategicArtifactTraceabilityNodeResolver nodeResolver,
            TraceabilityLinkRepository linkRepository
    ) {
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

    @Override
    public StrategicArtifactEvidenceSupport analyze(
            StrategicArtifact artifact
    ) {
        Objects.requireNonNull(
                artifact,
                "El artefacto estratégico es obligatorio"
        );

        TraceabilityNode strategicNode =
                nodeResolver.requireNode(
                        artifact
                );

        EvidenceLineagePath lineage =
                traverse(
                        artifact.getProject().getId(),
                        strategicNode,
                        0,
                        new HashSet<>()
                );

        if (!lineage.hasEvidence()) {
            return StrategicArtifactEvidenceSupport.none(
                    artifact,
                    "No existe una ruta DERIVED_FROM desde el artefacto " +
                    artifact.getArtifactCode() +
                    " hasta una evidencia fuente"
            );
        }

        if (lineage.weak()) {
            return StrategicArtifactEvidenceSupport.weak(
                    artifact,
                    lineage.evidenceCodes(),
                    lineage.weakestStrength(),
                    lineage.depth(),
                    "Existe evidencia trazable, pero al menos un enlace " +
                    "del lineage presenta soporte débil"
            );
        }

        if (lineage.depth() == 1) {
            return StrategicArtifactEvidenceSupport.direct(
                    artifact,
                    lineage.evidenceCodes(),
                    lineage.weakestStrength(),
                    "El artefacto deriva directamente de evidencia fuente"
            );
        }

        return StrategicArtifactEvidenceSupport.inherited(
                artifact,
                lineage.evidenceCodes(),
                lineage.weakestStrength(),
                lineage.depth(),
                "El artefacto hereda soporte documental mediante " +
                lineage.depth() +
                " relaciones DERIVED_FROM"
        );
    }

    private EvidenceLineagePath traverse(
            Long projectId,
            TraceabilityNode currentNode,
            int currentDepth,
            Set<NodeVisitKey> visited
    ) {
        if (currentDepth >= MAX_DEPTH) {
            return EvidenceLineagePath.none();
        }

        NodeVisitKey visitKey =
                new NodeVisitKey(
                        currentNode.getId(),
                        currentNode.getNodeCode()
                );

        if (!visited.add(visitKey)) {
            /*
             * Ciclo defensivo.
             *
             * La creación normal del grafo ya evita ciclos,
             * pero lineage no debe entrar en recursión infinita
             * si existen datos históricos inconsistentes.
             */
            return EvidenceLineagePath.none();
        }

        List<TraceabilityLink> links =
                linkRepository
                        .findAllByProjectIdAndSourceNodeIdAndRelationType(
                                projectId,
                                currentNode.getId(),
                                TraceabilityRelationType.DERIVED_FROM
                        );

        if (links.isEmpty()) {
            return EvidenceLineagePath.none();
        }

        Set<String> evidenceCodes =
                new TreeSet<>();

        TraceabilityStrength weakestStrength =
                null;

        int maximumDepth =
                0;

        boolean weak =
                false;

        for (TraceabilityLink link : links) {

            TraceabilityNode target =
                    link.getTargetNode();

            if (target == null) {
                continue;
            }

            TraceabilityStrength linkStrength =
                    link.getStrength();

            if (target.getNodeType()
                    == TraceabilityNodeType.SOURCE_EVIDENCE) {

                if (target.getExternalReference() != null
                        && !target.getExternalReference().isBlank()) {

                    evidenceCodes.add(
                            target.getExternalReference()
                    );
                }

                weakestStrength =
                        weakest(
                                weakestStrength,
                                linkStrength
                        );

                maximumDepth =
                        Math.max(
                                maximumDepth,
                                currentDepth + 1
                        );

                if (isWeak(linkStrength)) {
                    weak = true;
                }

                continue;
            }

            EvidenceLineagePath child =
                    traverse(
                            projectId,
                            target,
                            currentDepth + 1,
                            new HashSet<>(visited)
                    );

            if (!child.hasEvidence()) {
                continue;
            }

            evidenceCodes.addAll(
                    child.evidenceCodes()
            );

            TraceabilityStrength pathWeakest =
                    weakest(
                            linkStrength,
                            child.weakestStrength()
                    );

            weakestStrength =
                    weakest(
                            weakestStrength,
                            pathWeakest
                    );

            maximumDepth =
                    Math.max(
                            maximumDepth,
                            child.depth()
                    );

            if (child.weak()
                    || isWeak(linkStrength)) {
                weak = true;
            }
        }

        if (evidenceCodes.isEmpty()) {
            return EvidenceLineagePath.none();
        }

        return new EvidenceLineagePath(
                List.copyOf(evidenceCodes),
                weakestStrength,
                maximumDepth,
                weak
        );
    }

    private static TraceabilityStrength weakest(
            TraceabilityStrength first,
            TraceabilityStrength second
    ) {
        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return strengthRank(first)
                <= strengthRank(second)
                ? first
                : second;
    }

    /*
     * Usamos name() deliberadamente para no acoplar este
     * componente a todos los valores posibles del enum.
     *
     * Los valores conocidos actualmente son DIRECT,
     * STRONG y WEAK. Si existen otros, quedan ordenados
     * sensatamente mediante este ranking.
     */
    private static int strengthRank(
            TraceabilityStrength strength
    ) {
        if (strength == null) {
            return Integer.MIN_VALUE;
        }

        return switch (strength.name()) {

            case "DIRECT" ->
                    100;

            case "VERY_STRONG" ->
                    90;

            case "STRONG" ->
                    80;

            case "MODERATE", "MEDIUM" ->
                    60;

            case "WEAK" ->
                    20;

            default ->
                    50;
        };
    }

    private static boolean isWeak(
            TraceabilityStrength strength
    ) {
        return strength != null
                && strengthRank(strength) <= 20;
    }

    private record NodeVisitKey(
            Long id,
            String code
    ) {
    }
    
}
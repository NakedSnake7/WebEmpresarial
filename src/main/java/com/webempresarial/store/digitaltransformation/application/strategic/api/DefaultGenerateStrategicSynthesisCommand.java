package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class DefaultGenerateStrategicSynthesisCommand
        implements GenerateStrategicSynthesisCommand {

    private final StrategicGraphTraversalEngine
            traversalEngine;

    private final StrategicChainEvidenceCoverageEvaluator
            coverageEvaluator;

    private final StrategicSynthesisGate
            synthesisGate;

    private final StrategicSynthesisBuilder
            synthesisBuilder;

    private final StrategicSynthesisStore
            synthesisStore;

    public DefaultGenerateStrategicSynthesisCommand(
            StrategicGraphTraversalEngine traversalEngine,
            StrategicChainEvidenceCoverageEvaluator coverageEvaluator,
            StrategicSynthesisGate synthesisGate,
            StrategicSynthesisBuilder synthesisBuilder,
            StrategicSynthesisStore synthesisStore
    ) {
        this.traversalEngine =
                Objects.requireNonNull(
                        traversalEngine,
                        "StrategicGraphTraversalEngine es obligatorio"
                );

        this.coverageEvaluator =
                Objects.requireNonNull(
                        coverageEvaluator,
                        "StrategicChainEvidenceCoverageEvaluator es obligatorio"
                );

        this.synthesisGate =
                Objects.requireNonNull(
                        synthesisGate,
                        "StrategicSynthesisGate es obligatorio"
                );

        this.synthesisBuilder =
                Objects.requireNonNull(
                        synthesisBuilder,
                        "StrategicSynthesisBuilder es obligatorio"
                );

        this.synthesisStore =
                Objects.requireNonNull(
                        synthesisStore,
                        "StrategicSynthesisStore es obligatorio"
                );
    }

    @Override
    @Transactional
    public GenerateStrategicSynthesisResult generate(
            Long storeId,
            Long projectId,
            Long findingArtifactId
    ) {
        requirePositive(
                storeId,
                "storeId"
        );

        requirePositive(
                projectId,
                "projectId"
        );

        requirePositive(
                findingArtifactId,
                "findingArtifactId"
        );

        StrategicTraversalResult traversal =
                Objects.requireNonNull(
                        traversalEngine.traverseFromFinding(
                                storeId,
                                projectId,
                                findingArtifactId
                        ),
                        "StrategicGraphTraversalEngine devolvió un resultado nulo"
                );

        StrategicChain chain =
                traversal.toChain()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No fue posible construir una cadena estratégica"
                                )
                        );

        StrategicEvidenceCoverage coverage =
                Objects.requireNonNull(
                        coverageEvaluator.evaluate(
                                chain
                        ),
                        "StrategicChainEvidenceCoverageEvaluator devolvió un resultado nulo"
                );

        StrategicSynthesisGateResult gateResult =
                Objects.requireNonNull(
                        synthesisGate.evaluate(
                                traversal,
                                coverage
                        ),
                        "StrategicSynthesisGate devolvió un resultado nulo"
                );

        if (!gateResult.isEligible()) {
            throw new IllegalStateException(
                    "La cadena estratégica no está autorizada para síntesis automática"
            );
        }

        StrategicSynthesis synthesis =
                Objects.requireNonNull(
                        synthesisBuilder.build(
                                chain,
                                coverage,
                                gateResult
                        ),
                        "StrategicSynthesisBuilder devolvió una síntesis nula"
                );

        StoredStrategicSynthesis stored =
                resolveOrCreateSnapshot(
                        storeId,
                        projectId,
                        synthesis
                );

        return new GenerateStrategicSynthesisResult(
                stored,
                gateResult
        );
    }
    private StoredStrategicSynthesis resolveOrCreateSnapshot(
            Long storeId,
            Long projectId,
            StrategicSynthesis generated
    ) {
        return synthesisStore
                .findLatestSnapshot(
                        storeId,
                        projectId,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
                .filter(latest ->
                        canReuse(
                                latest.synthesis(),
                                generated
                        )
                )
                .orElseGet(() ->
                        Objects.requireNonNull(
                                synthesisStore.saveSnapshot(
                                        generated
                                ),
                                "StrategicSynthesisStore devolvió un snapshot nulo"
                        )
                );
    }

    private static boolean canReuse(
            StrategicSynthesis existing,
            StrategicSynthesis generated
    ) {
        if (existing == null) {
            return false;
        }

        /*
         * Solo reutilizamos una generación todavía READY.
         *
         * Si el último snapshot ya fue enviado a review,
         * aprobado o rechazado, Generate debe poder abrir
         * un nuevo ciclo creando un snapshot READY nuevo.
         */
        if (existing.getStatus()
                != StrategicSynthesisStatus.READY) {
            return false;
        }

        return existing.hasSameContentAs(
                generated
        );
    }
    private static void requirePositive(
            Long value,
            String name
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    name + " debe ser válido"
            );
        }
    }
}
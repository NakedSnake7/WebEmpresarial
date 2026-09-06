package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DefaultStrategicIntelligenceStateQuery
        implements StrategicIntelligenceStateQuery {

    private final StrategicGraphTraversalEngine
            traversalEngine;

    private final StrategicChainEvidenceCoverageEvaluator
            coverageEvaluator;

    private final StrategicSynthesisGate
            synthesisGate;

    private final StrategicSynthesisStore
            synthesisStore;

    private final StrategicSynthesisReviewStore
            reviewStore;

    private final StrategicIntelligenceCapabilitiesResolver
            capabilitiesResolver;

    public DefaultStrategicIntelligenceStateQuery(
            StrategicGraphTraversalEngine traversalEngine,
            StrategicChainEvidenceCoverageEvaluator coverageEvaluator,
            StrategicSynthesisGate synthesisGate,
            StrategicSynthesisStore synthesisStore,
            StrategicSynthesisReviewStore reviewStore,
            StrategicIntelligenceCapabilitiesResolver capabilitiesResolver
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

        this.synthesisStore =
                Objects.requireNonNull(
                        synthesisStore,
                        "StrategicSynthesisStore es obligatorio"
                );

        this.reviewStore =
                Objects.requireNonNull(
                        reviewStore,
                        "StrategicSynthesisReviewStore es obligatorio"
                );

        this.capabilitiesResolver =
                Objects.requireNonNull(
                        capabilitiesResolver,
                        "StrategicIntelligenceCapabilitiesResolver es obligatorio"
                );
    }
    
    private static Long resolveReviewableSynthesisId(
            StoredStrategicSynthesis deterministic,
            StoredStrategicSynthesis ai
    ) {
        /*
         * Una interpretación AI pendiente tiene prioridad,
         * porque requiere explícitamente una decisión humana.
         */
        if (isReviewable(ai)) {
            return ai.id();
        }

        /*
         * Una deterministic puede llegar aquí después
         * de Submit Review.
         */
        if (isReviewable(deterministic)) {
            return deterministic.id();
        }

        return null;
    }

    private static boolean isReviewable(
            StoredStrategicSynthesis stored
    ) {
        return stored != null
                && stored.synthesis() != null
                && stored.synthesis().getStatus()
                    == StrategicSynthesisStatus.REQUIRES_REVIEW;
    }

    @Override
    @Transactional(readOnly = true)
    public StrategicIntelligenceStateResponse findState(
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

        StrategicEvidenceCoverage coverage =
                resolveCoverage(
                        traversal
                );

        StrategicSynthesisGateResult gateResult =
                resolveGate(
                        traversal,
                        coverage
                );

        StoredStrategicSynthesis deterministic =
                synthesisStore
                        .findLatestSnapshot(
                                storeId,
                                projectId,
                                StrategicSynthesisOrigin.DETERMINISTIC
                        )
                        .orElse(
                                null
                        );

        StoredStrategicSynthesis ai =
                synthesisStore
                        .findLatestSnapshot(
                                storeId,
                                projectId,
                                StrategicSynthesisOrigin.AI_ASSISTED
                        )
                        .orElse(
                                null
                        );

        List<StoredStrategicSynthesisReview> reviews =
                Objects.requireNonNull(
                        reviewStore.findAllByProject(
                                storeId,
                                projectId
                        ),
                        "StrategicSynthesisReviewStore devolvió una lista nula"
                );

        StrategicIntelligenceCapabilitiesResponse capabilities =
                Objects.requireNonNull(
                        capabilitiesResolver.resolve(
                                traversal,
                                coverage,
                                gateResult,
                                deterministic,
                                ai
                        ),
                        "StrategicIntelligenceCapabilitiesResolver devolvió un resultado nulo"
                );
        
        

        Long reviewableSynthesisId =
                resolveReviewableSynthesisId(
                        deterministic,
                        ai
                );
        
        

        return new StrategicIntelligenceStateResponse(
                projectId,
                findingArtifactId,
                StrategicChainSummaryResponseMapper.toResponse(
                        traversal
                ),
                StrategicEvidenceCoverageResponseMapper.toResponse(
                        coverage
                ),
                StrategicSynthesisResponseMapper.toResponse(
                        deterministic
                ),
                StrategicSynthesisResponseMapper.toResponse(
                        ai
                ),
                reviews.stream()
                        .map(
                                StrategicReviewSummaryResponseMapper::toResponse
                        )
                        .toList(),
                capabilities,
                reviewableSynthesisId
        );
    }
    
    

    private StrategicEvidenceCoverage resolveCoverage(
            StrategicTraversalResult traversal
    ) {
        return traversal.toChain()
                .map(
                        coverageEvaluator::evaluate
                )
                .orElse(
                        null
                );
    }

    private StrategicSynthesisGateResult resolveGate(
            StrategicTraversalResult traversal,
            StrategicEvidenceCoverage coverage
    ) {
        if (coverage == null) {
            return null;
        }

        return Objects.requireNonNull(
                synthesisGate.evaluate(
                        traversal,
                        coverage
                ),
                "StrategicSynthesisGate devolvió un resultado nulo"
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
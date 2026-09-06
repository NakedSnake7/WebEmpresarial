package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class ReviewStrategicSynthesisService {

    private final StrategicSynthesisReviewPolicy reviewPolicy;

    private final StrategicSynthesisTraceabilityRegistrar
            traceabilityRegistrar;

    private final StrategicSynthesisGovernanceProvenanceRecorder
            provenanceRecorder;

    private final StrategicSynthesisStore synthesisStore;

    private final StrategicSynthesisReviewStore reviewStore;

    private final Clock clock;

    @Autowired
    public ReviewStrategicSynthesisService(
            StrategicSynthesisReviewPolicy reviewPolicy,
            StrategicSynthesisTraceabilityRegistrar traceabilityRegistrar,
            StrategicSynthesisGovernanceProvenanceRecorder provenanceRecorder,
            StrategicSynthesisStore synthesisStore,
            StrategicSynthesisReviewStore reviewStore
    ) {
        this(
                reviewPolicy,
                traceabilityRegistrar,
                provenanceRecorder,
                synthesisStore,
                reviewStore,
                Clock.systemUTC()
        );
    }

    ReviewStrategicSynthesisService(
            StrategicSynthesisReviewPolicy reviewPolicy,
            StrategicSynthesisTraceabilityRegistrar traceabilityRegistrar,
            StrategicSynthesisGovernanceProvenanceRecorder provenanceRecorder,
            StrategicSynthesisStore synthesisStore,
            StrategicSynthesisReviewStore reviewStore,
            Clock clock
    ) {
        this.reviewPolicy =
                Objects.requireNonNull(
                        reviewPolicy,
                        "StrategicSynthesisReviewPolicy es obligatorio"
                );

        this.traceabilityRegistrar =
                Objects.requireNonNull(
                        traceabilityRegistrar,
                        "StrategicSynthesisTraceabilityRegistrar es obligatorio"
                );

        this.provenanceRecorder =
                Objects.requireNonNull(
                        provenanceRecorder,
                        "StrategicSynthesisGovernanceProvenanceRecorder es obligatorio"
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

        this.clock =
                Objects.requireNonNull(
                        clock,
                        "Clock es obligatorio"
                );
    }

    @Transactional
    public ReviewStrategicSynthesisResult review(
            StoredStrategicSynthesis storedSynthesis,
            String reviewer,
            StrategicSynthesisReviewerType reviewerType,
            StrategicSynthesisReviewDecision decision,
            String reason
    ) {
        Objects.requireNonNull(
                storedSynthesis,
                "La síntesis persistida es obligatoria"
        );

        StrategicSynthesis synthesis =
                storedSynthesis.synthesis();

        Objects.requireNonNull(
                reviewerType,
                "El tipo de reviewer es obligatorio"
        );

        Objects.requireNonNull(
                decision,
                "La decisión es obligatoria"
        );

        StrategicSynthesisReviewPolicyResult policyResult =
                reviewPolicy.evaluate(
                        synthesis,
                        reviewerType,
                        decision
                );

        if (!policyResult.isAuthorized()) {

            if (policyResult.requiresHumanReview()) {
                throw new IllegalStateException(
                        "La revisión requiere intervención humana"
                );
            }

            throw new IllegalStateException(
                    "La revisión no está autorizada"
            );
        }

        Instant reviewedAt =
                Instant.now(
                        clock
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        synthesis,
                        reviewer,
                        reviewerType,
                        decision,
                        reason,
                        reviewedAt
                );

        StrategicSynthesis updated =
                synthesis.withStatus(
                        review.getResultingStatus()
                );

        /*
         * El resultado de governance es un nuevo snapshot.
         *
         * Nunca actualizamos destructivamente #42.
         */
        StoredStrategicSynthesis resultingSnapshot =
                synthesisStore.saveSnapshot(
                        updated
                );

        reviewStore.save(
                storedSynthesis.id(),
                resultingSnapshot.id(),
                review
        );

        /*
         * Conservamos la semántica actual de
         * traceability/provenance.
         */
        TraceabilityNode synthesisNode =
                traceabilityRegistrar.register(
                        synthesis
                );

        provenanceRecorder.recordReview(
                review,
                synthesisNode
        );

        return new ReviewStrategicSynthesisResult(
                resultingSnapshot.synthesis(),
                review,
                policyResult,
                review.getPreviousStatus(),
                review.getResultingStatus()
        );
    }
}
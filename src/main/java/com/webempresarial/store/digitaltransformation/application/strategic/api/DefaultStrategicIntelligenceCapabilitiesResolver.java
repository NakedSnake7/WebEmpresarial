package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultStrategicIntelligenceCapabilitiesResolver
        implements StrategicIntelligenceCapabilitiesResolver {

    private final StrategicAiAvailability
            aiAvailability;

    public DefaultStrategicIntelligenceCapabilitiesResolver(
            StrategicAiAvailability aiAvailability
    ) {
        this.aiAvailability =
                Objects.requireNonNull(
                        aiAvailability,
                        "StrategicAiAvailability es obligatorio"
                );
    }

    @Override
    public StrategicIntelligenceCapabilitiesResponse resolve(
            StrategicTraversalResult traversal,
            StrategicEvidenceCoverage coverage,
            StrategicSynthesisGateResult gateResult,
            StoredStrategicSynthesis deterministic,
            StoredStrategicSynthesis ai
    ) {
        Objects.requireNonNull(
                traversal,
                "StrategicTraversalResult es obligatorio"
        );

        boolean aiAvailable =
                aiAvailability.isAvailable();

        boolean canGenerateDeterministicSynthesis =
                coverage != null
                        && gateResult != null
                        && gateResult.isEligible();

        boolean canRequestAiInterpretation =
                aiAvailable
                        && deterministic != null
                        && canInterpret(
                                deterministic.synthesis()
                        );

        StrategicSynthesis reviewCandidate =
                selectReviewCandidate(
                        ai,
                        deterministic
                );

        boolean canSubmitForReview =
                reviewCandidate != null
                        && StrategicSynthesisLifecycle
                        .canSubmitForReview(
                                reviewCandidate.getStatus()
                        );

        boolean canReview =
                reviewCandidate != null
                        && StrategicSynthesisLifecycle
                        .canReview(
                                reviewCandidate.getStatus()
                        );

        return new StrategicIntelligenceCapabilitiesResponse(
                canGenerateDeterministicSynthesis,
                canRequestAiInterpretation,
                canSubmitForReview,
                canReview,
                canReview,
                aiAvailable
        );
    }

    private static boolean canInterpret(
            StrategicSynthesis synthesis
    ) {
        if (synthesis == null) {
            return false;
        }

        return synthesis.getOrigin()
                == StrategicSynthesisOrigin.DETERMINISTIC
                && (
                synthesis.getStatus()
                        == StrategicSynthesisStatus.READY
                        || synthesis.getStatus()
                        == StrategicSynthesisStatus.APPROVED
        );
    }

    private static StrategicSynthesis selectReviewCandidate(
            StoredStrategicSynthesis ai,
            StoredStrategicSynthesis deterministic
    ) {
        if (ai != null) {
            return ai.synthesis();
        }

        if (deterministic != null) {
            return deterministic.synthesis();
        }

        return null;
    }
}
package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultStrategicSynthesisConfidenceEvaluator
        implements StrategicSynthesisConfidenceEvaluator {

    @Override
    public StrategicSynthesisConfidence evaluate(
            StrategicChain chain,
            StrategicEvidenceCoverage evidenceCoverage
    ) {
        Objects.requireNonNull(
                chain,
                "La cadena estratégica es obligatoria"
        );

        Objects.requireNonNull(
                evidenceCoverage,
                "La cobertura de evidencia es obligatoria"
        );

        if (!chain.isComplete()) {
            return StrategicSynthesisConfidence.LOW;
        }

        return switch (evidenceCoverage.getStatus()) {

            case FULLY_SUPPORTED ->
                    StrategicSynthesisConfidence.HIGH;

            case MOSTLY_SUPPORTED ->
                    StrategicSynthesisConfidence.MEDIUM;

            case PARTIALLY_SUPPORTED,
                 WEAKLY_SUPPORTED,
                 UNSUPPORTED ->
                    StrategicSynthesisConfidence.LOW;
        };
    }
}
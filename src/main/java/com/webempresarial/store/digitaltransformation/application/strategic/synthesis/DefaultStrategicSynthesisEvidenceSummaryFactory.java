package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.TreeSet;

@Component
public class DefaultStrategicSynthesisEvidenceSummaryFactory
        implements StrategicSynthesisEvidenceSummaryFactory {

    @Override
    public StrategicSynthesisEvidenceSummary create(
            StrategicEvidenceCoverage coverage
    ) {
        Objects.requireNonNull(
                coverage,
                "La cobertura de evidencia es obligatoria"
        );

        TreeSet<String> evidenceCodes =
                new TreeSet<>();

        int maximumTraceDepth =
                0;

        for (StrategicArtifactEvidenceSupport support
                : coverage.getSupports()) {

            evidenceCodes.addAll(
                    support.getEvidenceCodes()
            );

            maximumTraceDepth =
                    Math.max(
                            maximumTraceDepth,
                            support.getTraceDepth()
                    );
        }

        return StrategicSynthesisEvidenceSummary.of(
                coverage.getStatus(),
                coverage.coveragePercentage(),
                evidenceCodes.stream().toList(),
                maximumTraceDepth
        );
    }
}
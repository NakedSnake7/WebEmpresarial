package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicArtifactEvidenceSupport;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChain;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicEvidenceCoverage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class DefaultStrategicChainEvidenceCoverageEvaluator
        implements StrategicChainEvidenceCoverageEvaluator {

    private final StrategicEvidenceLineageAnalyzer lineageAnalyzer;

    public DefaultStrategicChainEvidenceCoverageEvaluator(
            StrategicEvidenceLineageAnalyzer lineageAnalyzer
    ) {
        this.lineageAnalyzer =
                Objects.requireNonNull(
                        lineageAnalyzer,
                        "StrategicEvidenceLineageAnalyzer es obligatorio"
                );
    }

    @Override
    public StrategicEvidenceCoverage evaluate(
            StrategicChain chain
    ) {
        Objects.requireNonNull(
                chain,
                "La cadena estratégica es obligatoria"
        );

        List<StrategicArtifactEvidenceSupport> supports =
                new ArrayList<>();

        analyzeIfPresent(
                chain.getFinding(),
                supports
        );

        analyzeIfPresent(
                chain.getBusinessProblem(),
                supports
        );

        analyzeIfPresent(
                chain.getBusinessObjective(),
                supports
        );

        analyzeIfPresent(
                chain.getStrategicOpportunity(),
                supports
        );

        return StrategicEvidenceCoverage.of(
                chain,
                supports
        );
    }

    private void analyzeIfPresent(
            StrategicArtifact artifact,
            List<StrategicArtifactEvidenceSupport> supports
    ) {
        if (artifact == null) {
            return;
        }

        StrategicArtifactEvidenceSupport support =
                lineageAnalyzer.analyze(
                        artifact
                );

        if (support == null) {
            throw new IllegalStateException(
                    "StrategicEvidenceLineageAnalyzer devolvió un resultado nulo"
            );
        }

        if (support.getArtifact() != artifact) {
            throw new IllegalStateException(
                    "El soporte calculado no corresponde al artefacto solicitado"
            );
        }

        supports.add(support);
    }
}
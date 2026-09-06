package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SourceEvidenceStrategicCandidateMapperTest {

    private final SourceEvidenceStrategicCandidateMapper mapper =
            new SourceEvidenceStrategicCandidateMapper();

    @Test
    void shouldMapEvidenceIntoStrategicCandidate() {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                SourceEvidence.extract(
                        source.getProject(),
                        source,
                        null,
                        "EVD-AUDIT-001",
                        EvidenceClassification.STRATEGIC_FINDING,
                        EvidenceConfidence.EXPLICIT,
                        EvidenceExtractionOrigin.MANUAL,
                        "Existe una brecha digital.",
                        "Fragmento respaldatorio",
                        "La experiencia debe evolucionar.",
                        EvidenceLocator.page(2)
                );

        StrategicClassificationCandidate candidate =
                mapper.map(evidence);

        assertThat(candidate.statement())
                .isEqualTo(
                        "Existe una brecha digital."
                );

        assertThat(candidate.sourceClassification())
                .isEqualTo(
                        EvidenceClassification.STRATEGIC_FINDING
                );

        assertThat(candidate.sourceConfidence())
                .isEqualTo(
                        EvidenceConfidence.EXPLICIT
                );

        assertThat(candidate.rationale())
                .isEqualTo(
                        "La experiencia debe evolucionar."
                );
    }
}
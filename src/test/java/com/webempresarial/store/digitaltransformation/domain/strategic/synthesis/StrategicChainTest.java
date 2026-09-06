package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StrategicChainTest {

    @Test
    void shouldCreateCompleteStrategicChain() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        assertThat(chain.isComplete())
                .isTrue();

        assertThat(chain.getStatus())
                .isEqualTo(
                        StrategicChainStatus.COMPLETE
                );

        assertThat(chain.getCompleteness())
                .isEqualTo(
                        StrategicChainCompleteness.COMPLETE
                );

        assertThat(chain.completenessPercentage())
                .isEqualTo(100);

        assertThat(chain.getGaps())
                .isEmpty();
    }

    @Test
    void shouldDetectMissingObjectiveAndOpportunity() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        problem,
                        null,
                        null
                );

        assertThat(chain.isComplete())
                .isFalse();

        assertThat(chain.getCompleteness())
                .isEqualTo(
                        StrategicChainCompleteness.FINDING_AND_PROBLEM
                );

        assertThat(chain.completenessPercentage())
                .isEqualTo(50);

        assertThat(
                chain.hasGap(
                        StrategicChainGapType.MISSING_BUSINESS_OBJECTIVE
                )
        ).isTrue();

        assertThat(
                chain.hasGap(
                        StrategicChainGapType.MISSING_STRATEGIC_OPPORTUNITY
                )
        ).isTrue();
    }

    @Test
    void shouldRecognizeFindingOnlyChain() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicChain chain =
                StrategicChain.startingWith(
                        evidence.getProject(),
                        finding
                );

        assertThat(chain.getCompleteness())
                .isEqualTo(
                        StrategicChainCompleteness.FINDING_ONLY
                );

        assertThat(chain.completenessPercentage())
                .isEqualTo(25);

        assertThat(chain.getGaps())
                .hasSize(3);
    }

    @Test
    void shouldRecognizeNonCanonicalPartialChain() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        null,
                        objective,
                        null
                );

        assertThat(chain.getCompleteness())
                .isEqualTo(
                        StrategicChainCompleteness.PARTIAL_NON_CANONICAL
                );

        assertThat(chain.completenessPercentage())
                .isZero();

        assertThat(
                chain.hasGap(
                        StrategicChainGapType.MISSING_BUSINESS_PROBLEM
                )
        ).isTrue();
    }

    @Test
    void shouldRejectArtifactInWrongPosition() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        assertThatThrownBy(() ->
                StrategicChain.of(
                        evidence.getProject(),
                        objective,
                        null,
                        null,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "FINDING"
                );
    }

    @Test
    void shouldNotAllowDraftChainToBePrioritized() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicChain chain =
                completeChain(
                        evidence,
                        false
                );

        assertThat(chain.isComplete())
                .isTrue();

        assertThat(chain.canBePrioritized())
                .isFalse();
    }

    @Test
    void shouldAllowVerifiedChainToBePrioritized() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicChain chain =
                completeChain(
                        evidence,
                        true
                );

        assertThat(chain.canBePrioritized())
                .isTrue();
    }
    @Test
    void gapsShouldBeImmutable() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicChain chain =
                StrategicChain.startingWith(
                        evidence.getProject(),
                        finding
                );

        assertThatThrownBy(() ->
                chain.getGaps().clear()
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    private static StrategicChain completeChain(
            SourceEvidence evidence,
            boolean verify
    ) {
        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );

        if (verify) {
            finding.verify("tester");
            problem.verify("tester");
            objective.verify("tester");
            opportunity.verify("tester");
        }

        return StrategicChain.of(
                evidence.getProject(),
                finding,
                problem,
                objective,
                opportunity
        );
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            String code,
            StrategicArtifactType type
    ) {
        return StrategicArtifact.deriveFromEvidence(
                evidence.getProject(),
                evidence,
                code,
                type,
                StrategicConfidence.EXPLICIT,
                StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                "Strategic statement " + code,
                "Strategic rationale " + code,
                null
        );
    }
}
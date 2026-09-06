package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.classification.*;
import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicDerivationPolicyTest {

    private final DefaultStrategicDerivationPolicy policy =
            new DefaultStrategicDerivationPolicy();

    @Test
    void shouldAllowVerifiedAutoAcceptedEvidence() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        evidence.verify("tester");

        StrategicClassificationResult classification =
                classification(
                        StrategicClassificationDecision.AUTO_ACCEPT,
                        true
                );

        StrategicDerivationDecision result =
                policy.evaluate(
                        evidence,
                        classification
                );

        assertThat(result.action())
                .isEqualTo(
                        StrategicDerivationAction.DERIVE
                );

        assertThat(result.canDerive())
                .isTrue();
    }

    @Test
    void shouldRejectUnverifiedEvidence() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicDerivationDecision result =
                policy.evaluate(
                        evidence,
                        classification(
                                StrategicClassificationDecision.AUTO_ACCEPT,
                                true
                        )
                );

        assertThat(result.action())
                .isEqualTo(
                        StrategicDerivationAction.REJECT
                );
    }

    @Test
    void shouldRequireReviewWhenClassificationRequiresReview() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        evidence.verify("tester");

        StrategicDerivationDecision result =
                policy.evaluate(
                        evidence,
                        classification(
                                StrategicClassificationDecision.REVIEW_REQUIRED,
                                false
                        )
                );

        assertThat(result.action())
                .isEqualTo(
                        StrategicDerivationAction.REVIEW_REQUIRED
                );
    }

    private static StrategicClassificationResult classification(
            StrategicClassificationDecision decision,
            boolean eligible
    ) {
        return new StrategicClassificationResult(
                StrategicArtifactType.FINDING,
                StrategicConfidence.STRONGLY_SUPPORTED,
                decision,
                8,
                1,
                "Test",
                List.of(),
                decision != StrategicClassificationDecision.AUTO_ACCEPT,
                eligible
        );
    }
}
package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicAIResiliencePolicyTest {

    private final DefaultStrategicAIResiliencePolicy policy =
            new DefaultStrategicAIResiliencePolicy();

    @Test
    void shouldRetryFirstTransientFailure() {
        assertThat(
                policy.evaluate(
                        StrategicAIProviderFailure.TIMEOUT,
                        1
                )
        ).isEqualTo(
                StrategicAIResilienceDecision.RETRY
        );

        assertThat(
                policy.evaluate(
                        StrategicAIProviderFailure.RATE_LIMITED,
                        1
                )
        ).isEqualTo(
                StrategicAIResilienceDecision.RETRY
        );

        assertThat(
                policy.evaluate(
                        StrategicAIProviderFailure.PROVIDER_UNAVAILABLE,
                        1
                )
        ).isEqualTo(
                StrategicAIResilienceDecision.RETRY
        );
    }

    @Test
    void shouldFailClosedAfterSecondTransientFailure() {
        assertThat(
                policy.evaluate(
                        StrategicAIProviderFailure.TIMEOUT,
                        2
                )
        ).isEqualTo(
                StrategicAIResilienceDecision.FAIL_CLOSED
        );
    }

    @Test
    void shouldNeverRetryDeterministicFailures() {
        assertThat(
                policy.evaluate(
                        StrategicAIProviderFailure.AUTHENTICATION_FAILED,
                        1
                )
        ).isEqualTo(
                StrategicAIResilienceDecision.FAIL_CLOSED
        );

        assertThat(
                policy.evaluate(
                        StrategicAIProviderFailure.INVALID_RESPONSE,
                        1
                )
        ).isEqualTo(
                StrategicAIResilienceDecision.FAIL_CLOSED
        );

        assertThat(
                policy.evaluate(
                        StrategicAIProviderFailure.MALFORMED_STRUCTURED_OUTPUT,
                        1
                )
        ).isEqualTo(
                StrategicAIResilienceDecision.FAIL_CLOSED
        );

        assertThat(
                policy.evaluate(
                        StrategicAIProviderFailure.SAFETY_REFUSAL,
                        1
                )
        ).isEqualTo(
                StrategicAIResilienceDecision.FAIL_CLOSED
        );
    }

    @Test
    void shouldRejectInvalidAttemptNumber() {
        assertThatThrownBy(() ->
                policy.evaluate(
                        StrategicAIProviderFailure.TIMEOUT,
                        0
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }
}
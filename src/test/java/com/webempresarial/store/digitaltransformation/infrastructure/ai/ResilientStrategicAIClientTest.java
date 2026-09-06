package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResilientStrategicAIClientTest {

    @Mock
    private StrategicAIClient delegate;

    @Mock
    private StrategicAIResiliencePolicy policy;

    @Mock
    private StrategicAIRequest request;

    @Mock
    private StrategicAIResponse response;

    private ResilientStrategicAIClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        client =
                new ResilientStrategicAIClient(
                        delegate,
                        policy
                );
    }

    @Test
    void shouldReturnSuccessfulResponseWithoutConsultingPolicy() {
        when(
                delegate.generate(request)
        ).thenReturn(
                response
        );

        assertThat(
                client.generate(request)
        ).isSameAs(
                response
        );

        verify(
                delegate,
                times(1)
        ).generate(request);

        verifyNoInteractions(
                policy
        );
    }

    @Test
    void shouldRetryTransientFailureWhenAuthorized() {
        StrategicAIProviderException failure =
                new StrategicAIProviderException(
                        StrategicAIProviderFailure.TIMEOUT,
                        "Timeout"
                );

        when(
                delegate.generate(request)
        )
                .thenThrow(failure)
                .thenReturn(response);

        when(
                policy.evaluate(
                        StrategicAIProviderFailure.TIMEOUT,
                        1
                )
        ).thenReturn(
                StrategicAIResilienceDecision.RETRY
        );

        assertThat(
                client.generate(request)
        ).isSameAs(
                response
        );

        verify(
                delegate,
                times(2)
        ).generate(request);
    }

    @Test
    void shouldFailClosedWhenRetryIsNotAuthorized() {
        StrategicAIProviderException failure =
                new StrategicAIProviderException(
                        StrategicAIProviderFailure.INVALID_RESPONSE,
                        "Invalid response"
                );

        when(
                delegate.generate(request)
        ).thenThrow(
                failure
        );

        when(
                policy.evaluate(
                        StrategicAIProviderFailure.INVALID_RESPONSE,
                        1
                )
        ).thenReturn(
                StrategicAIResilienceDecision.FAIL_CLOSED
        );

        assertThatThrownBy(() ->
                client.generate(request)
        )
                .isSameAs(
                        failure
                );

        verify(
                delegate,
                times(1)
        ).generate(request);
    }

    @Test
    void shouldTreatNullProviderResponseAsInvalidResponse() {
        when(
                delegate.generate(request)
        ).thenReturn(
                null
        );

        when(
                policy.evaluate(
                        StrategicAIProviderFailure.INVALID_RESPONSE,
                        1
                )
        ).thenReturn(
                StrategicAIResilienceDecision.FAIL_CLOSED
        );

        assertThatThrownBy(() ->
                client.generate(request)
        )
                .isInstanceOf(
                        StrategicAIProviderException.class
                )
                .satisfies(error ->
                        assertThat(
                                ((StrategicAIProviderException) error)
                                        .getFailure()
                        ).isEqualTo(
                                StrategicAIProviderFailure.INVALID_RESPONSE
                        )
                );
    }
}
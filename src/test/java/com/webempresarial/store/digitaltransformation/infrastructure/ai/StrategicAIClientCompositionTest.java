package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import com.webempresarial.store.digitaltransformation.infrastructure.ai.openai.OpenAiProperties;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.openai.OpenAiStructuredResponseGateway;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StrategicAIClientCompositionTest {

    @Test
    void shouldComposeProviderClientBehindResilienceBoundary() {
        OpenAiProperties properties =
                new OpenAiProperties();

        properties.setEnabled(true);
        properties.setApiKey("test-key");
        properties.setModel("test-model");
        properties.setMaxOutputTokens(1200);

        OpenAiStructuredResponseGateway gateway =
                mock(
                        OpenAiStructuredResponseGateway.class
                );

        StrategicAIResiliencePolicy resiliencePolicy =
                new DefaultStrategicAIResiliencePolicy();

        StrategicAIConfiguration configuration =
                new StrategicAIConfiguration();

        StrategicAIClient client =
                configuration.strategicAIClient(
                        properties,
                        gateway,
                        resiliencePolicy
                );

        assertThat(client)
                .isInstanceOf(
                        ResilientStrategicAIClient.class
                );
    }
}
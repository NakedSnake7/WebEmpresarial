package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.openai.client.OpenAIClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenAiSdkStructuredResponseGatewayTest {

    @Mock
    private OpenAIClient client;

    private OpenAiProperties properties;

    private OpenAiSdkStructuredResponseGateway gateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        properties =
                new OpenAiProperties();

        properties.setEnabled(
                true
        );

        properties.setApiKey(
                "test-key"
        );

        properties.setModel(
                "test-model"
        );

        properties.setMaxOutputTokens(
                1200
        );

        gateway =
                new OpenAiSdkStructuredResponseGateway(
                        client,
                        properties
                );
    }

    @Test
    void shouldRejectBlankSystemInstructionBeforeCallingSdk() {
        assertThatThrownBy(() ->
                gateway.generate(
                        " ",
                        "INPUT"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "sistema"
                );

        verifyNoInteractions(
                client
        );
    }

    @Test
    void shouldRejectBlankInputBeforeCallingSdk() {
        assertThatThrownBy(() ->
                gateway.generate(
                        "SYSTEM",
                        " "
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "input"
                );

        verifyNoInteractions(
                client
        );
    }

    @Test
    void shouldRejectInvalidPropertiesAtConstructionTime() {
        OpenAiProperties invalid =
                new OpenAiProperties();

        invalid.setEnabled(
                true
        );

        assertThatThrownBy(() ->
                new OpenAiSdkStructuredResponseGateway(
                        client,
                        invalid
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "API key"
                );
    }
}
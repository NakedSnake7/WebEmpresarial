package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIRequest;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenAiStrategicAIClientTest {

    @Mock
    private OpenAiStructuredResponseGateway gateway;

    private OpenAiStrategicAIClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        client =
                new OpenAiStrategicAIClient(
                        gateway
                );
    }

    @Test
    void shouldTranslateStructuredOpenAiOutputIntoNeutralResponse() {
        StrategicAIRequest request =
                request();

        OpenAiStrategicOutput output =
                new OpenAiStrategicOutput();

        output.interpretedThesis =
                "Refined strategic thesis";

        output.executiveNarrative =
                "Executive narrative";

        output.referencedArtifactCodes =
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                );

        when(
                gateway.generate(
                        eq(request.systemInstruction()),
                        anyString()
                )
        ).thenReturn(
                output
        );

        StrategicAIResponse response =
                client.generate(
                        request
                );

        assertThat(response.interpretedThesis())
                .isEqualTo(
                        "Refined strategic thesis"
                );

        assertThat(response.executiveNarrative())
                .isEqualTo(
                        "Executive narrative"
                );

        assertThat(response.referencedArtifactCodes())
                .containsExactly(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                );

        verify(gateway)
        .generate(
                eq(request.systemInstruction()),
                argThat(input ->
                        input.contains("Finding")
                                && input.contains("Business problem")
                                && input.contains("Business objective")
                                && input.contains("Strategic opportunity")
                                && input.contains("Deterministic thesis")
                                && input.contains("FND-001")
                                && input.contains("PRB-001")
                                && input.contains("OBJ-001")
                                && input.contains("OPP-001")
                                && input.contains("DO_NOT_INTRODUCE_NEW_FACTS")
                                && input.contains("DO_NOT_INTRODUCE_NEW_OBJECTIVES")
                )
        );
    }

    @Test
    void shouldWrapProviderFailure() {
        StrategicAIRequest request =
                request();

        when(
                gateway.generate(
                        anyString(),
                        anyString()
                )
        ).thenThrow(
                new RuntimeException(
                        "provider unavailable"
                )
        );

        assertThatThrownBy(() ->
                client.generate(
                        request
                )
        )
                .isInstanceOf(
                        OpenAiStrategicAIException.class
                )
                .hasMessageContaining(
                        "OpenAI"
                )
                .hasCauseInstanceOf(
                        RuntimeException.class
                );
    }

    @Test
    void shouldRejectNullStructuredResponse() {
        when(
                gateway.generate(
                        anyString(),
                        anyString()
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                client.generate(
                        request()
                )
        )
                .isInstanceOf(
                        OpenAiStrategicAIException.class
                )
                .hasMessageContaining(
                        "nula"
                );
    }

    @Test
    void shouldRejectBlankThesisFromProvider() {
        OpenAiStrategicOutput output =
                new OpenAiStrategicOutput();

        output.interpretedThesis =
                " ";

        when(
                gateway.generate(
                        anyString(),
                        anyString()
                )
        ).thenReturn(
                output
        );

        assertThatThrownBy(() ->
                client.generate(
                        request()
                )
        )
                .isInstanceOf(
                        OpenAiStrategicAIException.class
                )
                .hasMessageContaining(
                        "tesis"
                );
    }
    
    @Test
    void shouldRejectNullRequestBeforeCallingGateway() {

        assertThatThrownBy(() ->
                client.generate(
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "StrategicAIRequest"
                );

        verifyNoInteractions(
                gateway
        );
    }
    @Test
    void shouldNormalizeNullReferencedArtifactCodesToEmptyList() {

        OpenAiStrategicOutput output =
                new OpenAiStrategicOutput();

        output.interpretedThesis =
                "Refined strategic thesis";

        output.executiveNarrative =
                "Executive narrative";

        output.referencedArtifactCodes =
                null;

        when(
                gateway.generate(
                        anyString(),
                        anyString()
                )
        ).thenReturn(
                output
        );

        StrategicAIResponse response =
                client.generate(
                        request()
                );

        assertThat(
                response.referencedArtifactCodes()
        ).isEmpty();
    }
    

    private static StrategicAIRequest request() {
        return new StrategicAIRequest(
                "SYSTEM",
                "TASK",
                "Finding",
                "Business problem",
                "Business objective",
                "Strategic opportunity",
                "Deterministic thesis",
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                ),
                List.of(
                        "DO_NOT_INTRODUCE_NEW_FACTS",
                        "DO_NOT_INTRODUCE_NEW_OBJECTIVES"
                )
        );
    }
}
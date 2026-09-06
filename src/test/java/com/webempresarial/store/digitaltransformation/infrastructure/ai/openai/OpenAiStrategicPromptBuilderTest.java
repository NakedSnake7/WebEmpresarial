package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiStrategicPromptBuilderTest {

    @Test
    void shouldIncludeAuthorizedStrategicContext() {
        StrategicAIRequest request =
                new StrategicAIRequest(
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
                                "DO_NOT_INTRODUCE_NEW_OBJECTIVES",
                                "DO_NOT_INTRODUCE_NEW_OPPORTUNITIES"
                        )
                );

        String input =
                OpenAiStrategicPromptBuilder.buildInput(
                        request
                );

        assertThat(input)
                .contains(
                        "TASK"
                )
                .contains(
                        "Finding"
                )
                .contains(
                        "Business problem"
                )
                .contains(
                        "Business objective"
                )
                .contains(
                        "Strategic opportunity"
                )
                .contains(
                        "Deterministic thesis"
                )
                .contains(
                        "FND-001"
                )
                .contains(
                        "PRB-001"
                )
                .contains(
                        "OBJ-001"
                )
                .contains(
                        "OPP-001"
                );
    }

    @Test
    void shouldIncludeStrategicGuardrails() {
        StrategicAIRequest request =
                new StrategicAIRequest(
                        "SYSTEM",
                        "TASK",
                        "Finding",
                        "Business problem",
                        "Business objective",
                        "Strategic opportunity",
                        "Deterministic thesis",
                        List.of(
                                "FND-001"
                        ),
                        List.of(
                                "DO_NOT_INTRODUCE_NEW_FACTS",
                                "DO_NOT_INTRODUCE_NEW_OBJECTIVES",
                                "DO_NOT_INTRODUCE_NEW_OPPORTUNITIES"
                        )
                );

        String input =
                OpenAiStrategicPromptBuilder.buildInput(
                        request
                );

        assertThat(input)
                .contains(
                        "DO_NOT_INTRODUCE_NEW_FACTS"
                )
                .contains(
                        "DO_NOT_INTRODUCE_NEW_OBJECTIVES"
                )
                .contains(
                        "DO_NOT_INTRODUCE_NEW_OPPORTUNITIES"
                )
                .containsIgnoringCase(
                        "authorized source artifact"
                );
    }
}
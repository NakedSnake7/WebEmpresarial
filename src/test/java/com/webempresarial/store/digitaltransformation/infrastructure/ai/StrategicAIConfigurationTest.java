package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import com.openai.client.OpenAIClient;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpretationGuardrailValidator;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpretationOrchestrator;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpretationPromptPolicy;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpretationTraceabilityRecorder;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpreter;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.openai.OpenAiProperties;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.openai.OpenAiSdkConfiguration;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.openai.OpenAiStructuredResponseGateway;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StrategicAIConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            StrategicAIConfiguration.class,
                            TestDependencies.class
                    );

    @Test
    void shouldNotCreateAiPipelineWhenOpenAiIsDisabled() {

        contextRunner
                .withPropertyValues(
                        "webempresarial.ai.openai.enabled=false"
                )
                .run(context -> {

                    assertThat(
                            context.getBeansOfType(
                                    StrategicAIClient.class
                            )
                    ).isEmpty();

                    assertThat(
                            context.getBeansOfType(
                                    StrategicInterpreter.class
                            )
                    ).isEmpty();

                    assertThat(
                            context.getBeansOfType(
                                    StrategicInterpretationOrchestrator.class
                            )
                    ).isEmpty();
                });
    }

    @Test
    void shouldCreateAiPipelineWhenProviderIsAvailable() {

        contextRunner
                .withPropertyValues(
                        "webempresarial.ai.openai.enabled=true",
                        "webempresarial.ai.openai.api-key=test-key",
                        "webempresarial.ai.openai.model=test-model",
                        "webempresarial.ai.openai.max-output-tokens=1200"
                )
                .withBean(
                        OpenAiStructuredResponseGateway.class,
                        () ->
                                mock(
                                        OpenAiStructuredResponseGateway.class
                                )
                )
                .run(context -> {

                    assertThat(context)
                            .hasSingleBean(
                                    StrategicAIClient.class
                            );

                    assertThat(context)
                            .hasSingleBean(
                                    StrategicInterpreter.class
                            );

                    assertThat(context)
                            .hasSingleBean(
                                    StrategicInterpretationOrchestrator.class
                            );

                    StrategicAIClient aiClient =
                            context.getBean(
                                    StrategicAIClient.class
                            );

                    assertThat(aiClient)
                            .isInstanceOf(
                                    ResilientStrategicAIClient.class
                            );

                    StrategicInterpreter interpreter =
                            context.getBean(
                                    StrategicInterpreter.class
                            );

                    assertThat(interpreter)
                            .isInstanceOf(
                                    ProviderNeutralStrategicInterpreter.class
                            );
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestDependencies {

        @Bean
        StrategicAIResiliencePolicy resiliencePolicy() {
            return mock(
                    StrategicAIResiliencePolicy.class
            );
        }

        @Bean
        StrategicInterpretationPromptPolicy promptPolicy() {
            return mock(
                    StrategicInterpretationPromptPolicy.class
            );
        }

        @Bean
        StrategicInterpretationGuardrailValidator guardrailValidator() {
            return mock(
                    StrategicInterpretationGuardrailValidator.class
            );
        }

        @Bean
        StrategicInterpretationTraceabilityRecorder traceabilityRecorder() {
            return mock(
                    StrategicInterpretationTraceabilityRecorder.class
            );
        }
    }
}
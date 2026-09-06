package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.openai.OpenAiProperties;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.openai.OpenAiStrategicAIClient;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.openai.OpenAiStructuredResponseGateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
        OpenAiProperties.class
)
public class StrategicAIConfiguration {

    /*
     * =========================================================
     * PROVIDER
     * =========================================================
     *
     * OpenAI es únicamente una implementación concreta de
     * StrategicAIClient.
     *
     * Si OpenAI está deshabilitado, este bean no existe.
     */
	@Bean
	@ConditionalOnProperty(
	        prefix = "webempresarial.ai.openai",
	        name = "enabled",
	        havingValue = "true"
	)
	public StrategicAIClient strategicAIClient(
	        OpenAiProperties properties,
	        OpenAiStructuredResponseGateway gateway,
	        StrategicAIResiliencePolicy resiliencePolicy
	) {
	    properties.validate();

	    OpenAiStrategicAIClient providerClient =
	            new OpenAiStrategicAIClient(
	                    gateway
	            );

	    return new ResilientStrategicAIClient(
	            providerClient,
	            resiliencePolicy
	    );
	}

    /*
     * =========================================================
     * PROVIDER-NEUTRAL INTERPRETER
     * =========================================================
     *
     * StrategicInterpreter existe solamente cuando algún
     * StrategicAIClient está disponible.
     *
     * Ese cliente podría ser:
     *
     * OpenAI
     * Anthropic
     * Gemini
     * Ollama
     * Local Model
     * etc.
     */
    @Bean
    @ConditionalOnBean(
            StrategicAIClient.class
    )
    public StrategicInterpreter strategicInterpreter(
            StrategicAIClient aiClient,
            StrategicInterpretationPromptPolicy promptPolicy
    ) {
        return new ProviderNeutralStrategicInterpreter(
                aiClient,
                promptPolicy
        );
    }

    /*
     * =========================================================
     * ORCHESTRATOR
     * =========================================================
     *
     * El orchestrator solo existe cuando ya existe un
     * StrategicInterpreter operativo.
     */
    @Bean
    @ConditionalOnBean(
            StrategicInterpreter.class
    )
    public StrategicInterpretationOrchestrator
    strategicInterpretationOrchestrator(
            StrategicInterpreter interpreter,
            StrategicInterpretationGuardrailValidator guardrailValidator,
            StrategicInterpretationTraceabilityRecorder traceabilityRecorder
    ) {
        return new StrategicInterpretationOrchestrator(
                interpreter,
                guardrailValidator,
                traceabilityRecorder
        );
    }
}
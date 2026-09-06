package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "webempresarial.ai.openai",
        name = "enabled",
        havingValue = "true"
)
public class OpenAiSdkConfiguration {

    @Bean
    public OpenAIClient openAIClient(
            OpenAiProperties properties
    ) {
        properties.validate();

        return OpenAIOkHttpClient.builder()
                .apiKey(
                        properties.getApiKey()
                )
                .build();
    }

    @Bean
    public OpenAiStructuredResponseGateway
    openAiStructuredResponseGateway(
            OpenAIClient client,
            OpenAiProperties properties
    ) {
        return new OpenAiSdkStructuredResponseGateway(
                client,
                properties
        );
    }
}
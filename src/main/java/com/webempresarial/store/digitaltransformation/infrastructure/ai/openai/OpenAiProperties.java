package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "webempresarial.ai.openai"
)
public class OpenAiProperties {

    private boolean enabled = false;

    private String apiKey;

    private String model = "gpt-5.6";

    private int maxOutputTokens = 1200;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = normalize(apiKey);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = normalize(model);
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(
            int maxOutputTokens
    ) {
        if (maxOutputTokens <= 0) {
            throw new IllegalArgumentException(
                    "maxOutputTokens debe ser mayor que cero"
            );
        }

        this.maxOutputTokens =
                maxOutputTokens;
    }

    public void validate() {
        if (!enabled) {
            return;
        }

        if (apiKey == null) {
            throw new IllegalStateException(
                    "La API key de OpenAI es obligatoria " +
                    "cuando el proveedor está habilitado"
            );
        }

        if (model == null) {
            throw new IllegalStateException(
                    "El modelo de OpenAI es obligatorio"
            );
        }
    }

    private static String normalize(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }
}
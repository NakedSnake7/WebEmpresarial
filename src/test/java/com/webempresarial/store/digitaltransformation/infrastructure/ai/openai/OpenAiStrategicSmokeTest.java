package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public final class OpenAiStrategicSmokeTest {

    private OpenAiStrategicSmokeTest() {
    }

    public static void main(String[] args) {

        String apiKey =
                System.getenv(
                        "OPENAI_API_KEY"
                );

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY no está configurada"
            );
        }

        OpenAiProperties properties =
                new OpenAiProperties();

        properties.setEnabled(true);
        properties.setApiKey(apiKey);

        String model =
                System.getenv(
                        "OPENAI_MODEL"
                );

        if (model != null && !model.isBlank()) {
            properties.setModel(
                    model
            );
        }

        properties.setMaxOutputTokens(
                1200
        );

        properties.validate();

        OpenAIClient client =
                OpenAIOkHttpClient.builder()
                        .apiKey(
                                properties.getApiKey()
                        )
                        .build();

        try {

            OpenAiStructuredResponseGateway gateway =
                    new OpenAiSdkStructuredResponseGateway(
                            client,
                            properties
                    );

            OpenAiStrategicAIClient strategicClient =
                    new OpenAiStrategicAIClient(
                            gateway
                    );

            var request =
                    new com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIRequest(
                            """
                            You are a strategic interpretation component.
                            Preserve the supplied strategic meaning.
                            Do not introduce new facts, objectives or opportunities.
                            """,
                            """
                            Refine the deterministic thesis without changing
                            its strategic meaning.
                            """,
                            "The existing digital experience does not clearly communicate the business value.",
                            "Potential customers struggle to understand the company's differentiated offering.",
                            "Improve clarity of the digital value proposition.",
                            "Use a clearer strategic narrative to increase understanding and trust.",
                            "The digital experience should translate the company's differentiated value into a clearer customer-facing strategic narrative.",
                            java.util.List.of(
                                    "FND-SMOKE-001",
                                    "PRB-SMOKE-001",
                                    "OBJ-SMOKE-001",
                                    "OPP-SMOKE-001"
                            ),
                            java.util.List.of(
                                    "PRESERVE_FACTUAL_MEANING",
                                    "DO_NOT_INTRODUCE_NEW_FACTS",
                                    "DO_NOT_INTRODUCE_NEW_OBJECTIVES",
                                    "DO_NOT_INTRODUCE_NEW_OPPORTUNITIES",
                                    "REQUIRE_SOURCE_ALIGNMENT"
                            )
                    );

            var response =
                    strategicClient.generate(
                            request
                    );

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "OPENAI STRATEGIC SMOKE TEST"
            );

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "Thesis:"
            );

            System.out.println(
                    response.interpretedThesis()
            );

            System.out.println();

            System.out.println(
                    "Executive Narrative:"
            );

            System.out.println(
                    response.executiveNarrative()
            );

            System.out.println();

            System.out.println(
                    "Referenced artifacts:"
            );

            System.out.println(
                    response.referencedArtifactCodes()
            );

            System.out.println(
                    "========================================"
            );

        } finally {

            if (client instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception exception) {
                    System.err.println(
                            "No fue posible cerrar OpenAIClient: "
                                    + exception.getMessage()
                    );
                }
            }
        }
    }
}
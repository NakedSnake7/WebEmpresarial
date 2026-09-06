package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;

import java.util.List;

public final class StrategicInterpretationEndToEndSmokeTest {

    private StrategicInterpretationEndToEndSmokeTest() {
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

        /*
         * =========================================================
         * OPENAI CONFIGURATION
         * =========================================================
         */

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

        OpenAIClient openAIClient =
                OpenAIOkHttpClient.builder()
                        .apiKey(
                                properties.getApiKey()
                        )
                        .build();

        try {

            /*
             * =========================================================
             * PROVIDER INFRASTRUCTURE
             * =========================================================
             */

            OpenAiStructuredResponseGateway gateway =
                    new OpenAiSdkStructuredResponseGateway(
                            openAIClient,
                            properties
                    );

            StrategicAIClient providerClient =
                    new OpenAiStrategicAIClient(
                            gateway
                    );

            StrategicAIResiliencePolicy resiliencePolicy =
                    new DefaultStrategicAIResiliencePolicy();

            StrategicAIClient resilientClient =
                    new ResilientStrategicAIClient(
                            providerClient,
                            resiliencePolicy
                    );

            /*
             * =========================================================
             * PROVIDER-NEUTRAL INTERPRETER
             * =========================================================
             */

            StrategicInterpretationPromptPolicy promptPolicy =
                    new DefaultStrategicInterpretationPromptPolicy();

            StrategicInterpreter interpreter =
                    new ProviderNeutralStrategicInterpreter(
                            resilientClient,
                            promptPolicy
                    );

            /*
             * =========================================================
             * GUARDRAILS
             * =========================================================
             */

            StrategicInterpretationGuardrailValidator
                    guardrailValidator =
                    new DefaultStrategicInterpretationGuardrailValidator();

            /*
             * =========================================================
             * TRACEABILITY
             * =========================================================
             *
             * Para este smoke test NO persistimos todavía.
             * Queremos probar OpenAI + interpreter + guardrails +
             * synthesis lifecycle.
             *
             * La persistencia real ya está cubierta por los tests
             * de StrategicInterpretationTraceabilityRecorder.
             */

            StrategicInterpretationTraceabilityRecorder
                    traceabilityRecorder =
                    new StrategicInterpretationTraceabilityRecorder() {

                        @Override
                        public void record(
                                StrategicSynthesis sourceSynthesis,
                                StrategicSynthesis aiSynthesis,
                                StrategicInterpretationAudit audit
                        ) {
                            System.out.println();
                            System.out.println(
                                    "[TRACEABILITY]"
                            );

                            System.out.println(
                                    "sourceOrigin="
                                            + sourceSynthesis.getOrigin()
                            );

                            System.out.println(
                                    "aiOrigin="
                                            + aiSynthesis.getOrigin()
                            );

                            System.out.println(
                                    "validation="
                                            + audit.getValidationStatus()
                            );

                            System.out.println(
                                    "mode="
                                            + audit.getMode()
                            );

                            System.out.println(
                                    "sourceArtifacts="
                                            + audit.getSourceArtifactCodes()
                            );

                            System.out.println(
                                    "referencedArtifacts="
                                            + audit.getReferencedArtifactCodes()
                            );
                        }
                    };

            /*
             * =========================================================
             * ORCHESTRATOR
             * =========================================================
             */

            StrategicInterpretationOrchestrator orchestrator =
                    new StrategicInterpretationOrchestrator(
                            interpreter,
                            guardrailValidator,
                            traceabilityRecorder
                    );

            /*
             * =========================================================
             * DETERMINISTIC SYNTHESIS
             * =========================================================
             */

            StrategicSynthesis deterministic =
                    deterministicSynthesis();

            /*
             * =========================================================
             * REAL AI INTERPRETATION
             * =========================================================
             */

            StrategicInterpretationOutcome outcome =
                    orchestrator.interpret(
                            deterministic,
                            StrategicInterpretationMode.REFINE_THESIS
                    );

            /*
             * =========================================================
             * FAIL-CLOSED ASSERTIONS
             * =========================================================
             */

            require(
                    outcome.validation().isValid(),
                    "La interpretación AI no superó los guardrails"
            );

            require(
                    outcome.synthesis().getOrigin()
                            == StrategicSynthesisOrigin.AI_ASSISTED,
                    "La síntesis resultante debe ser AI_ASSISTED"
            );

            require(
                    outcome.synthesis().getStatus()
                            == StrategicSynthesisStatus.REQUIRES_REVIEW,
                    "La síntesis AI debe quedar en REQUIRES_REVIEW"
            );

            require(
                    deterministic.getOrigin()
                            == StrategicSynthesisOrigin.DETERMINISTIC,
                    "La síntesis original fue modificada incorrectamente"
            );

            require(
                    deterministic.getStatus()
                            == StrategicSynthesisStatus.READY,
                    "La síntesis determinista debe permanecer READY"
            );

            require(
                    outcome.synthesis()
                            .getSourceArtifactCodes()
                            .equals(
                                    deterministic
                                            .getSourceArtifactCodes()
                            ),
                    "Los artefactos fuente no fueron preservados"
            );

            require(
                    deterministic
                            .getSourceArtifactCodes()
                            .containsAll(
                                    outcome.interpretation()
                                            .getReferencedArtifactCodes()
                            ),
                    "La IA referenció artefactos no autorizados"
            );

            /*
             * =========================================================
             * OUTPUT
             * =========================================================
             */

            System.out.println();
            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "STRATEGIC INTERPRETATION END-TO-END SMOKE TEST"
            );

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "Deterministic thesis:"
            );

            System.out.println(
                    deterministic.getStrategicThesis()
            );

            System.out.println();

            System.out.println(
                    "AI thesis:"
            );

            System.out.println(
                    outcome.synthesis()
                            .getStrategicThesis()
            );

            System.out.println();

            System.out.println(
                    "Origin:"
            );

            System.out.println(
                    outcome.synthesis().getOrigin()
            );

            System.out.println();

            System.out.println(
                    "Status:"
            );

            System.out.println(
                    outcome.synthesis().getStatus()
            );

            System.out.println();

            System.out.println(
                    "Validation:"
            );

            System.out.println(
                    outcome.validation().getStatus()
            );

            System.out.println();

            System.out.println(
                    "Referenced artifacts:"
            );

            System.out.println(
                    outcome.interpretation()
                            .getReferencedArtifactCodes()
            );

            System.out.println();

            System.out.println(
                    "Guardrail violations:"
            );

            System.out.println(
                    outcome.validation()
                            .getViolations()
            );

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "END-TO-END AI GOVERNANCE: SUCCESS"
            );

            System.out.println(
                    "=================================================="
            );

        } finally {

            if (openAIClient instanceof AutoCloseable closeable) {
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

    private static StrategicSynthesis deterministicSynthesis() {

        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),

                "The current digital experience does not clearly communicate "
                        + "the differentiated business value.",

                "Potential customers struggle to understand why the company "
                        + "is meaningfully different from alternative providers.",

                "Improve clarity and understanding of the digital value "
                        + "proposition.",

                "Translate the differentiated business value into a clearer "
                        + "customer-facing strategic narrative that strengthens trust.",

                "The digital experience should communicate the company's "
                        + "differentiated value through a clear customer-facing "
                        + "strategic narrative.",

                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-SMOKE-001"
                        ),
                        4
                ),

                StrategicSynthesisConfidence.HIGH,

                StrategicSynthesisOrigin.DETERMINISTIC,

                StrategicSynthesisStatus.READY,

                List.of(
                        "FND-SMOKE-001",
                        "PRB-SMOKE-001",
                        "OBJ-SMOKE-001",
                        "OPP-SMOKE-001"
                )
        );
    }

    private static void require(
            boolean condition,
            String message
    ) {
        if (!condition) {
            throw new IllegalStateException(
                    message
            );
        }
    }
}
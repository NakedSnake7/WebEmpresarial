package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIRequest;

import java.util.Objects;

public final class OpenAiStrategicPromptBuilder {

    private OpenAiStrategicPromptBuilder() {
    }

    public static String buildInput(
            StrategicAIRequest request
    ) {
        Objects.requireNonNull(
                request,
                "StrategicAIRequest es obligatorio"
        );

        return """
                TASK
                ----
                %s

                AUTHORIZED STRATEGIC CONTEXT
                ----------------------------

                FINDING
                %s

                BUSINESS PROBLEM
                %s

                BUSINESS OBJECTIVE
                %s

                STRATEGIC OPPORTUNITY
                %s

                DETERMINISTIC THESIS
                %s

                AUTHORIZED SOURCE ARTIFACTS
                %s

                REQUIRED CONSTRAINTS
                %s

                OUTPUT REQUIREMENTS
                -------------------
                - interpretedThesis must contain the refined strategic thesis.
                - executiveNarrative may provide a concise executive explanation.
                - referencedArtifactCodes must contain only identifiers from the
                  authorized source artifact list.
                """.formatted(
                request.taskInstruction(),
                request.finding(),
                request.businessProblem(),
                request.businessObjective(),
                request.strategicOpportunity(),
                request.deterministicThesis(),
                String.join(
                        ", ",
                        request.sourceArtifactCodes()
                ),
                String.join(
                        ", ",
                        request.constraints()
                )
        ).trim();
    }
}
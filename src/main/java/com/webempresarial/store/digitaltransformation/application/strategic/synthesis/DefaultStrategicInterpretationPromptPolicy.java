package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultStrategicInterpretationPromptPolicy
        implements StrategicInterpretationPromptPolicy {

    @Override
    public String systemInstruction() {
        return """
                You are a strategic interpretation component
                operating inside WebEmpresarial Consulting.

                Your task is to improve the clarity and strategic
                expression of already validated strategic knowledge.

                You must not introduce new facts.
                You must not create new business objectives.
                You must not create new strategic opportunities.
                You must preserve the meaning of the supplied
                finding, business problem, business objective
                and strategic opportunity.

                The supplied source artifact identifiers define
                the complete authorized strategic context.

                Return only an interpretation supported by that context.
                """.trim();
    }

    @Override
    public String taskInstruction(
            StrategicInterpretationRequest request
    ) {
        Objects.requireNonNull(
                request,
                "StrategicInterpretationRequest es obligatorio"
        );

        return switch (request.getMode()) {

            case REFINE_THESIS ->
                    """
                    Refine the supplied deterministic strategic thesis.
                    Improve clarity, executive quality and strategic
                    coherence without changing its meaning.
                    """.trim();

            case EXECUTIVE_SYNTHESIS ->
                    """
                    Produce an executive-quality strategic synthesis
                    using only the supplied authorized strategic context.
                    """.trim();

            case STRATEGIC_NARRATIVE ->
                    """
                    Express the supplied strategy as a concise,
                    coherent strategic narrative while preserving
                    all authorized strategic meaning.
                    """.trim();
        };
    }
}
package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicInterpretationPromptPolicyTest {

    private final DefaultStrategicInterpretationPromptPolicy policy =
            new DefaultStrategicInterpretationPromptPolicy();

    @Test
    void systemInstructionShouldForbidNewStrategicKnowledge() {
        String instruction =
                policy.systemInstruction();

        assertThat(instruction)
                .containsIgnoringCase(
                        "must not introduce new facts"
                )
                .containsIgnoringCase(
                        "must not create new business objectives"
                )
                .containsIgnoringCase(
                        "must not create new strategic opportunities"
                );
    }
}
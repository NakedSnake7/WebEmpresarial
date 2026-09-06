package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationRequest;

public interface StrategicInterpretationPromptPolicy {

    String systemInstruction();

    String taskInstruction(
            StrategicInterpretationRequest request
    );
}
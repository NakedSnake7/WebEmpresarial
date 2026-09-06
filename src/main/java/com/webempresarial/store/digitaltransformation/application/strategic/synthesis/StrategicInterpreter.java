package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationRequest;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationResult;

public interface StrategicInterpreter {

    StrategicInterpretationResult interpret(
            StrategicInterpretationRequest request
    );
}
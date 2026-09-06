package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

public interface StrategicInterpretationGuardrailValidator {

    StrategicInterpretationValidationResult validate(
            StrategicInterpretationRequest request,
            StrategicInterpretationResult result
    );
}
package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationMode;

public interface RequestStrategicInterpretationCommand {

    RequestStrategicInterpretationResult interpret(
            Long storeId,
            Long projectId,
            StrategicInterpretationMode mode
    );
}
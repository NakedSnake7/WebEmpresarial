package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChain;

public interface StrategicThesisGenerator {

    String generate(
            StrategicChain chain
    );
}
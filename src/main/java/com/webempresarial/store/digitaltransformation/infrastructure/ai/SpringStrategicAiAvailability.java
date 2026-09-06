package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import com.webempresarial.store.digitaltransformation.application.strategic.api.StrategicAiAvailability;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpreter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class SpringStrategicAiAvailability
        implements StrategicAiAvailability {

    private final ObjectProvider<StrategicInterpreter>
            interpreterProvider;

    public SpringStrategicAiAvailability(
            ObjectProvider<StrategicInterpreter> interpreterProvider
    ) {
        this.interpreterProvider =
                interpreterProvider;
    }

    @Override
    public boolean isAvailable() {
        return interpreterProvider.getIfAvailable()
                != null;
    }
}
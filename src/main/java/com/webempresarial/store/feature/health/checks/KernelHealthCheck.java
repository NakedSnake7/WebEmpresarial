package com.webempresarial.store.feature.health.checks;

import com.webempresarial.store.feature.health.HealthCheck;
import com.webempresarial.store.feature.health.HealthResult;

import org.springframework.stereotype.Component;

@Component
public class KernelHealthCheck implements HealthCheck {

    @Override
    public HealthResult check() {
        return HealthResult.up(
                "Kernel",
                "Platform Kernel operativo"
        );
    }
}
package com.webempresarial.store.feature.registry;

import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.health.HealthCheck;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HealthRegistry {

    private final List<Class<? extends HealthCheck>> healthChecks = new ArrayList<>();

    public void register(PlatformModuleDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }

        healthChecks.addAll(descriptor.getHealthChecks());
    }

    public List<Class<? extends HealthCheck>> all() {
        return List.copyOf(healthChecks);
    }
}
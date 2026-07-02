package com.webempresarial.store.feature.health;

import com.webempresarial.store.feature.registry.HealthRegistry;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthEngine {

    private final HealthRegistry healthRegistry;
    private final ApplicationContext applicationContext;

    public HealthEngine(
            HealthRegistry healthRegistry,
            ApplicationContext applicationContext
    ) {
        this.healthRegistry = healthRegistry;
        this.applicationContext = applicationContext;
    }

    public List<HealthResult> checkAll() {
        return healthRegistry.all()
                .stream()
                .map(this::execute)
                .toList();
    }

    private HealthResult execute(Class<? extends HealthCheck> checkClass) {
        try {
            HealthCheck check = applicationContext.getBean(checkClass);
            return check.check();
        } catch (Exception ex) {
            return HealthResult.down(
                    checkClass.getSimpleName(),
                    ex.getMessage()
            );
        }
    }
}
package com.webempresarial.store.feature.registry;

import com.webempresarial.store.feature.automation.AutomationDefinition;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AutomationRegistry {

    private final List<AutomationDefinition> automations = new ArrayList<>();

    public void register(AutomationDefinition automation) {
        if (automation != null) {
            automations.add(automation);
        }
    }

    public List<AutomationDefinition> all() {
        return List.copyOf(automations);
    }
}
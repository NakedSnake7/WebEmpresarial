package com.webempresarial.store.feature.automation.conditions;

import com.webempresarial.store.feature.automation.AutomationCondition;
import com.webempresarial.store.feature.automation.AutomationContext;

import org.springframework.stereotype.Component;

@Component
public class AlwaysTrueCondition implements AutomationCondition {

    @Override
    public boolean matches(AutomationContext context) {
        return true;
    }
}
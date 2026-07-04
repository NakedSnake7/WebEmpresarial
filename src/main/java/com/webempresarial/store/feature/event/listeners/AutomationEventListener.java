package com.webempresarial.store.feature.event.listeners;

import com.webempresarial.store.feature.automation.AutomationEngine;
import com.webempresarial.store.feature.event.PlatformEvent;
import com.webempresarial.store.feature.event.PlatformEventListener;
import com.webempresarial.store.feature.runtime.ExecutionContext;

import org.springframework.stereotype.Component;

@Component
public class AutomationEventListener implements PlatformEventListener {

    private final AutomationEngine automationEngine;

    public AutomationEventListener(AutomationEngine automationEngine) {
        this.automationEngine = automationEngine;
    }

    @Override
    public boolean supports(String eventName) {
        return true;
    }

    @Override
    public void handle(PlatformEvent event) {
        ExecutionContext automationContext =
                ExecutionContext.childOf(event.executionContext());

        automationEngine.fire(
                event.name(),
                automationContext,
                event.payload(),
                event.metadata()
        );
    }
}
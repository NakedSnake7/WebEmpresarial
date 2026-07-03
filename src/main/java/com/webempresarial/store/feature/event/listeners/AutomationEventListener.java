package com.webempresarial.store.feature.event.listeners;

import com.webempresarial.store.feature.automation.AutomationEngine;
import com.webempresarial.store.feature.event.PlatformEvent;
import com.webempresarial.store.feature.event.PlatformEventListener;

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
        automationEngine.fire(
                event.name(),
                event.payload(),
                event.metadata()
        );
    }
}
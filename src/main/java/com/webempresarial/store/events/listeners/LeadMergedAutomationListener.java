package com.webempresarial.store.events.listeners;

import com.webempresarial.store.events.LeadMergedEvent;
import com.webempresarial.store.feature.automation.AutomationEngine;
import com.webempresarial.store.feature.automation.AutomationTrigger;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LeadMergedAutomationListener {

    private final AutomationEngine automationEngine;

    public LeadMergedAutomationListener(
            AutomationEngine automationEngine
    ) {
        this.automationEngine = automationEngine;
    }

    @EventListener
    public void onLeadMerged(LeadMergedEvent event) {
        automationEngine.fire(
                AutomationTrigger.LEAD_MERGED,
                event,
                Map.of(
                        "sourceLeadId", event.getSource().getId(),
                        "targetLeadId", event.getTarget().getId(),
                        "strategy", event.getStrategy().name()
                )
        );
    }
}
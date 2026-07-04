package com.webempresarial.store.feature.automation.crm;

import com.webempresarial.store.feature.automation.AutomationAction;
import com.webempresarial.store.feature.automation.AutomationContext;
import com.webempresarial.store.feature.automation.AutomationExecutionResult;
import com.webempresarial.store.service.notification.TraceNotificationService;

import org.springframework.stereotype.Component;

@Component
public class NotifyNewLeadAction implements AutomationAction {

    private final TraceNotificationService traceNotificationService;

    public NotifyNewLeadAction(TraceNotificationService traceNotificationService) {
        this.traceNotificationService = traceNotificationService;
    }

    @Override
    public AutomationExecutionResult execute(AutomationContext context) {
        traceNotificationService.notifyNewLead(context);

        return AutomationExecutionResult.success(
                "Notificación de nuevo lead enviada"
        );
    }
}
package com.webempresarial.store.service.notification;

import com.webempresarial.store.feature.automation.AutomationContext;
import com.webempresarial.store.feature.runtime.ExecutionTracer;

import org.springframework.stereotype.Service;

@Service
public class TraceNotificationService {

    private final ExecutionTracer executionTracer;

    public TraceNotificationService(ExecutionTracer executionTracer) {
        this.executionTracer = executionTracer;
    }

    public void notifyNewLead(AutomationContext context) {
        executionTracer
                .service(
                        context.scope(),
                        "NotificationService.notifyNewLead",
                        "Notification"
                )
                .run(() -> {
                    System.out.println("[Notification] Nuevo lead notificado");
                });
    }
}
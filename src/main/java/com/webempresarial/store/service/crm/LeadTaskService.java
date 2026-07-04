package com.webempresarial.store.service.crm;

import com.webempresarial.store.feature.automation.AutomationContext; 
import com.webempresarial.store.feature.runtime.ExecutionTracer;

import org.springframework.stereotype.Service;

@Service
public class LeadTaskService {

    private final ExecutionTracer executionTracer;

    public LeadTaskService(ExecutionTracer executionTracer) {
        this.executionTracer = executionTracer;
    }

    public void createTaskForNewLead(AutomationContext context) {
    	executionTracer
        .service(
                context.scope(),
                "LeadTaskService.createTaskForNewLead",
                "CRM"
        )
        .run(() -> {
            System.out.println("[CRM] Tarea comercial creada para nuevo lead");
        });
    }
}
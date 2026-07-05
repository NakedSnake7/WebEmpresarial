package com.webempresarial.store.service.crm;

import com.webempresarial.store.feature.automation.AutomationContext; 
import com.webempresarial.store.feature.runtime.ExecutionTracer;
import com.webempresarial.store.feature.runtime.annotations.TraceService;
import com.webempresarial.store.service.trace.TraceRepositoryService;

import org.springframework.stereotype.Service;

@Service
public class LeadTaskService {

    private final ExecutionTracer executionTracer;

    private final TraceRepositoryService traceRepositoryService;

    public LeadTaskService(
            ExecutionTracer executionTracer,
            TraceRepositoryService traceRepositoryService
    ) {
        this.executionTracer = executionTracer;
        this.traceRepositoryService = traceRepositoryService;
    }
    @TraceService(
            name = "LeadTaskService.createTaskForNewLead",
            source = "CRM"
    )
    public void createTaskForNewLead(AutomationContext context) {
        System.out.println("[CRM] Tarea comercial creada para nuevo lead");

        traceRepositoryService.saveLeadTask();
        traceRepositoryService.saveLeadActivity();
    }
}
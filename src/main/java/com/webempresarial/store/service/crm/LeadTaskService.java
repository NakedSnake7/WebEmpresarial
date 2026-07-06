package com.webempresarial.store.service.crm;

import com.webempresarial.store.feature.automation.AutomationContext;  
import com.webempresarial.store.feature.runtime.TraceType;
import com.webempresarial.store.feature.runtime.annotations.Trace;
import com.webempresarial.store.service.trace.TraceRepositoryService;

import org.springframework.stereotype.Service;

@Service
public class LeadTaskService {


    private final TraceRepositoryService traceRepositoryService;

    public LeadTaskService(
           
            TraceRepositoryService traceRepositoryService
    ) {
      
        this.traceRepositoryService = traceRepositoryService;
    }
    @Trace(
            type = TraceType.SERVICE,
            name = "LeadTaskService.createTaskForNewLead",
            source = "CRM"
    )
    public void createTaskForNewLead(AutomationContext context) {
        System.out.println("[CRM] Tarea comercial creada para nuevo lead");

        traceRepositoryService.saveLeadTask();
        traceRepositoryService.saveLeadActivity();
    }
}
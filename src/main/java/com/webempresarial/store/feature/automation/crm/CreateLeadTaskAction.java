package com.webempresarial.store.feature.automation.crm;

import com.webempresarial.store.feature.automation.AutomationAction;
import com.webempresarial.store.feature.automation.AutomationContext;
import com.webempresarial.store.feature.automation.AutomationExecutionResult;
import com.webempresarial.store.service.crm.LeadTaskService;

import org.springframework.stereotype.Component;

@Component
public class CreateLeadTaskAction implements AutomationAction {

    private final LeadTaskService leadTaskService;

    public CreateLeadTaskAction(LeadTaskService leadTaskService) {
        this.leadTaskService = leadTaskService;
    }

    @Override
    public AutomationExecutionResult execute(AutomationContext context) {
        leadTaskService.createTaskForNewLead(context);

        return AutomationExecutionResult.success(
                "Tarea comercial creada para nuevo lead"
        );
    }
}
package com.webempresarial.store.service;

import com.webempresarial.store.entity.AutomationExecution;
import com.webempresarial.store.entity.AutomationExecutionAction;
import com.webempresarial.store.feature.automation.ActionExecutionResult;
import com.webempresarial.store.feature.automation.AutomationExecutionReport;
import com.webempresarial.store.repository.AutomationExecutionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AutomationHistoryService {

    private final AutomationExecutionRepository automationExecutionRepository;

    public AutomationHistoryService(
            AutomationExecutionRepository automationExecutionRepository
    ) {
        this.automationExecutionRepository = automationExecutionRepository;
    }

    @Transactional
    public void save(AutomationExecutionReport report) {

        if (report == null || report.actions().isEmpty()) {
            return;
        }

        AutomationExecution execution = new AutomationExecution();
        
        execution.setCorrelationId(
                report.executionContext().correlationId()
        );

        execution.setExecutionId(
                report.executionContext().executionId()
        );
        execution.setParentExecutionId(
                report.executionContext().parentExecutionId()
        );

        execution.setSpanId(
                report.executionContext().spanId()
        );

        execution.setTriggerName(report.trigger());
        execution.setStartedAt(report.startedAt());
        execution.setFinishedAt(LocalDateTime.now());
        execution.setSuccess(report.success());
        execution.setTotalActions(report.actions().size());

        long totalDuration = report.actions()
                .stream()
                .mapToLong(ActionExecutionResult::executionTimeMs)
                .sum();

        execution.setTotalDurationMs(totalDuration);

        for (ActionExecutionResult result : report.actions()) {
            AutomationExecutionAction action = new AutomationExecutionAction();

            action.setActionName(result.action());
            action.setSuccess(result.success());
            action.setMessage(result.message());
            action.setDurationMs(result.executionTimeMs());

            execution.addAction(action);
        }

        automationExecutionRepository.save(execution);
    }
}
package com.webempresarial.store.feature.automation;

import com.webempresarial.store.feature.registry.AutomationRegistry;
import com.webempresarial.store.feature.runtime.ExecutionContext;
import com.webempresarial.store.service.AutomationHistoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AutomationEngine {

    private static final Logger log =
            LoggerFactory.getLogger(AutomationEngine.class);

    private final AutomationRegistry automationRegistry;
    private final ApplicationContext applicationContext;
    private final AutomationHistoryService automationHistoryService;

    public AutomationEngine(
            AutomationRegistry automationRegistry,
            ApplicationContext applicationContext,
            AutomationHistoryService automationHistoryService
    ) {
        this.automationRegistry = automationRegistry;
        this.applicationContext = applicationContext;
        this.automationHistoryService = automationHistoryService;
    }

    public AutomationExecutionReport fire(String trigger, Object payload) {
        return fire(trigger, payload, Map.of());
    }

    public AutomationExecutionReport fire(
            String trigger,
            Object payload,
            Map<String, Object> metadata
    ) {
        return fire(
                trigger,
                new ExecutionContext(),
                payload,
                metadata
        );
    }

    public AutomationExecutionReport fire(
            String trigger,
            ExecutionContext executionContext,
            Object payload,
            Map<String, Object> metadata
    ) {
        AutomationContext context = new AutomationContext(
                trigger,
                executionContext,
                payload,
                metadata
        );

        return execute(context);
    }

    private AutomationExecutionReport execute(AutomationContext context) {
    	AutomationExecutionReport report =
    	        new AutomationExecutionReport(
    	                context.trigger(),
    	                context.executionContext()
    	        );

        List<AutomationDefinition> automations =
                automationRegistry.findByTrigger(context.trigger());

        automations.forEach(automation -> {
            boolean matches = automation.conditions()
                    .stream()
                    .map(applicationContext::getBean)
                    .allMatch(condition -> condition.matches(context));

            if (!matches) {
                return;
            }

            automation.actions().forEach(actionClass -> executeAction(
                    actionClass,
                    context,
                    report
            ));
        });

        automationHistoryService.save(report);

        return report;
    }

    private void executeAction(
            Class<? extends AutomationAction> actionClass,
            AutomationContext context,
            AutomationExecutionReport report
    ) {
        long start = System.currentTimeMillis();

        try {
            AutomationAction action =
                    applicationContext.getBean(actionClass);

            AutomationExecutionResult result =
                    action.execute(context);

            report.add(new ActionExecutionResult(
                    actionClass.getSimpleName(),
                    result.success(),
                    result.message(),
                    System.currentTimeMillis() - start
            ));

        } catch (Exception ex) {
            report.add(new ActionExecutionResult(
                    actionClass.getSimpleName(),
                    false,
                    ex.getMessage(),
                    System.currentTimeMillis() - start
            ));

            log.error(
                    "Error ejecutando automation action: {}",
                    actionClass.getSimpleName(),
                    ex
            );
        }
    }
}
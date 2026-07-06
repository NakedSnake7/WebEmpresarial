package com.webempresarial.store.service.trace;

import com.webempresarial.store.feature.runtime.ExecutionScope;
import com.webempresarial.store.feature.runtime.ExecutionScopeHolder;
import com.webempresarial.store.feature.runtime.ExecutionTracer;
import com.webempresarial.store.feature.runtime.TraceType;
import com.webempresarial.store.feature.runtime.annotations.Trace;

import org.springframework.stereotype.Service;

@Service
public class TraceRepositoryService {

    private final ExecutionTracer executionTracer;

    public TraceRepositoryService(ExecutionTracer executionTracer) {
        this.executionTracer = executionTracer;
    }

    @Trace(
            type = TraceType.REPOSITORY,name = "SalesTaskRepository.save", source = "MySQL")
    public void saveLeadTask() {
        System.out.println("[Repository] SalesTask guardada");
    }

    public void saveLeadActivity() {
        ExecutionScope scope = ExecutionScopeHolder.current();
        
        if (scope == null) {
            System.out.println("[Trace] No hay ExecutionScope activo para LeadActivityRepository.save");
            return;
        }
        executionTracer
                .repository(scope, "LeadActivityRepository.save", "MySQL")
                .run(() -> System.out.println("[Repository] Actividad del lead guardada"));
    }
}
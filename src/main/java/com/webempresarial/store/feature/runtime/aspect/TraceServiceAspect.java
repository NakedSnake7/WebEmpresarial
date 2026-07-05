package com.webempresarial.store.feature.runtime.aspect;

import com.webempresarial.store.feature.automation.AutomationContext;
import com.webempresarial.store.feature.runtime.ExecutionScope;
import com.webempresarial.store.feature.runtime.ExecutionTracer;
import com.webempresarial.store.feature.runtime.annotations.TraceService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TraceServiceAspect {

    private final ExecutionTracer executionTracer;

    public TraceServiceAspect(ExecutionTracer executionTracer) {
        this.executionTracer = executionTracer;
    }

    @Around("@annotation(traceService)")
    public Object trace(
            ProceedingJoinPoint joinPoint,
            TraceService traceService
    ) throws Throwable {

        ExecutionScope scope = findScope(joinPoint.getArgs());

        if (scope == null) {
            return joinPoint.proceed();
        }

        String name = !traceService.name().isBlank()
                ? traceService.name()
                : joinPoint.getSignature().toShortString();

        try {
            return executionTracer
                    .service(scope, name, traceService.source())
                    .getWithScope(childScope -> {
                        try {
                            return joinPoint.proceed();
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    });

        } catch (RuntimeException ex) {
            if (ex.getCause() != null) {
                throw ex.getCause();
            }

            throw ex;
        }
    }

    private ExecutionScope findScope(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ExecutionScope scope) {
                return scope;
            }

            if (arg instanceof AutomationContext context) {
                return context.scope();
            }
        }

        return null;
    }
}
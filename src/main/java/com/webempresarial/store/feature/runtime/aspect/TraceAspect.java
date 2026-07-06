package com.webempresarial.store.feature.runtime.aspect;

import com.webempresarial.store.feature.automation.AutomationContext;
import com.webempresarial.store.feature.runtime.*;
import com.webempresarial.store.feature.runtime.annotations.Trace;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TraceAspect {

    private final ExecutionTracer executionTracer;

    public TraceAspect(ExecutionTracer executionTracer) {
        this.executionTracer = executionTracer;
    }

    @Around("@annotation(trace)")
    public Object trace(
            ProceedingJoinPoint joinPoint,
            Trace trace
    ) throws Throwable {

        ExecutionScope scope = resolveScope(joinPoint.getArgs());

        if (scope == null && trace.rootIfMissing()) {
            scope = ExecutionScope.root();
        }

        if (scope == null) {
            return joinPoint.proceed();
        }

        String name = !trace.name().isBlank()
                ? trace.name()
                : joinPoint.getSignature().toShortString();

        try {
            return executionTracer
                    .operation(
                            trace.type(),
                            scope,
                            name,
                            trace.source()
                    )
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

    private ExecutionScope resolveScope(Object[] args) {
        ExecutionScope current = ExecutionScopeHolder.current();

        if (current != null) {
            return current;
        }

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
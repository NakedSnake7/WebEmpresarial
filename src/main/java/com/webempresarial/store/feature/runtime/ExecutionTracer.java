package com.webempresarial.store.feature.runtime;

import com.webempresarial.store.service.ExecutionSpanService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class ExecutionTracer {

    private final ExecutionSpanService executionSpanService;

    public ExecutionTracer(ExecutionSpanService executionSpanService) {
        this.executionSpanService = executionSpanService;
    }

    public TraceOperation action(ExecutionScope parentScope, String name) {
        return new TraceOperation(parentScope, "ACTION", name, "AutomationEngine");
    }

    public TraceOperation service(ExecutionScope parentScope, String name, String source) {
        return new TraceOperation(parentScope, "SERVICE", name, source);
    }

    public TraceOperation repository(ExecutionScope parentScope, String name, String source) {
        return new TraceOperation(parentScope, "REPOSITORY", name, source);
    }

    public TraceOperation http(ExecutionScope parentScope, String name, String source) {
        return new TraceOperation(parentScope, "HTTP", name, source);
    }

    public TraceOperation span(ExecutionScope parentScope, String name, String source) {
        return new TraceOperation(parentScope, "SPAN", name, source);
    }

    public class TraceOperation {

        private final ExecutionScope parentScope;
        private final String type;
        private final String name;
        private final String source;

        private TraceOperation(
                ExecutionScope parentScope,
                String type,
                String name,
                String source
        ) {
            this.parentScope = parentScope;
            this.type = type;
            this.name = name;
            this.source = source;
        }

        public void run(Runnable runnable) {
            get(() -> {
                runnable.run();
                return null;
            });
        }

        public <T> T get(Supplier<T> supplier) {
            ExecutionScope childScope = parentScope.child();
            ExecutionContext context = childScope.context();

            LocalDateTime startedAt = LocalDateTime.now();
            long start = System.currentTimeMillis();

            try {
                T result = supplier.get();

                executionSpanService.save(
                        context,
                        type,
                        name,
                        source,
                        true,
                        "OK",
                        startedAt,
                        LocalDateTime.now(),
                        System.currentTimeMillis() - start
                );

                return result;

            } catch (Exception ex) {
                executionSpanService.save(
                        context,
                        type,
                        name,
                        source,
                        false,
                        ex.getMessage(),
                        startedAt,
                        LocalDateTime.now(),
                        System.currentTimeMillis() - start
                );

                throw ex;
            }
        }
        public void runWithScope(Consumer<ExecutionScope> consumer) {
            getWithScope(childScope -> {
                consumer.accept(childScope);
                return null;
            });
        }

        public <T> T getWithScope(java.util.function.Function<ExecutionScope, T> supplier) {
            ExecutionScope childScope = parentScope.child();
            ExecutionContext context = childScope.context();

            LocalDateTime startedAt = LocalDateTime.now();
            long start = System.currentTimeMillis();

            ExecutionScope previous = ExecutionScopeHolder.current();
            ExecutionScopeHolder.set(childScope);

            try {
                T result = supplier.apply(childScope);

                executionSpanService.save(
                        context,
                        type,
                        name,
                        source,
                        true,
                        "OK",
                        startedAt,
                        LocalDateTime.now(),
                        System.currentTimeMillis() - start
                );

                return result;

            } catch (Exception ex) {
                executionSpanService.save(
                        context,
                        type,
                        name,
                        source,
                        false,
                        ex.getMessage(),
                        startedAt,
                        LocalDateTime.now(),
                        System.currentTimeMillis() - start
                );

                throw ex;

            } finally {
                if (previous != null) {
                    ExecutionScopeHolder.set(previous);
                } else {
                    ExecutionScopeHolder.clear();
                }
            }
        }
    }
    public TraceOperation operation(
            TraceType type,
            ExecutionScope parentScope,
            String name,
            String source
    ) {
        return new TraceOperation(
                parentScope,
                type.name(),
                name,
                source
        );
    }
}
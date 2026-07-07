package com.webempresarial.store.feature.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webempresarial.store.feature.automation.AutomationContext;
import com.webempresarial.store.service.ExecutionSpanService;

import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class ExecutionTracer {

    private final ExecutionSpanService executionSpanService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public TraceOperation operation(
            TraceType type,
            ExecutionScope parentScope,
            String name,
            String source
    ) {
        return new TraceOperation(parentScope, type.name(), name, source);
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
            return execute(null, childScope -> supplier.get());
        }

        public void runWithScope(Consumer<ExecutionScope> consumer) {
            execute(null, childScope -> {
                consumer.accept(childScope);
                return null;
            });
        }

        public <T> T getWithScope(Function<ExecutionScope, T> supplier) {
            return execute(null, supplier);
        }
        public <T> T getWithScope(Object input, Function<ExecutionScope, T> supplier) {
            return execute(input, supplier);
        }
        private <T> T execute(Object input, Function<ExecutionScope, T> supplier) {
            ExecutionScope childScope = parentScope.child();
            ExecutionContext context = childScope.context();

            LocalDateTime startedAt = LocalDateTime.now();
            long start = System.currentTimeMillis();

            ExecutionScope previous = ExecutionScopeHolder.current();
            ExecutionScopeHolder.set(childScope);

            try {
                T result = supplier.apply(childScope);

                saveSuccess(
                        context,
                        startedAt,
                        LocalDateTime.now(),
                        System.currentTimeMillis() - start,
                        input,
                        result
                );

                return result;

            } catch (RuntimeException ex) {
            	saveFailure(
            	        context,
            	        startedAt,
            	        LocalDateTime.now(),
            	        System.currentTimeMillis() - start,
            	        input,
            	        ex
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

        private void saveSuccess(
                ExecutionContext context,
                LocalDateTime startedAt,
                LocalDateTime finishedAt,
                long durationMs,
                Object input,
                Object result
        ){
            executionSpanService.save(new ExecutionSpanRecord(
                    context,
                    type,
                    name,
                    source,
                    true,
                    "OK",
                    startedAt,
                    finishedAt,
                    durationMs,
                    extractPayload(input),
                    extractMetadata(input),
                    serialize(input),
                    serialize(result),
                    null,
                    null,
                    null
            ));
        }

        private void saveFailure(
                ExecutionContext context,
                LocalDateTime startedAt,
                LocalDateTime finishedAt,
                long durationMs,
                Object input,
                Exception ex
        ) {
            executionSpanService.save(new ExecutionSpanRecord(
                    context,
                    type,
                    name,
                    source,
                    false,
                    ex.getMessage(),
                    startedAt,
                    finishedAt,
                    durationMs,
                    extractPayload(input),
                    extractMetadata(input),
                    serialize(input),
                    null,
                    ex.getClass().getName(),
                    ex.getMessage(),
                    stacktrace(ex)
            ));
        }

        private String serialize(Object value) {
            if (value == null) {
                return null;
            }

            try {
                return objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(normalize(value));
            } catch (Exception ex) {
                return String.valueOf(value);
            }
        }

        private Object normalize(Object value) {
            if (value == null) {
                return null;
            }

            if (value instanceof Object[] array) {
                return java.util.Arrays.stream(array)
                        .map(this::normalize)
                        .toList();
            }

            if (value instanceof com.webempresarial.store.feature.automation.AutomationContext context) {
                return java.util.Map.of(
                        "trigger", context.trigger(),
                        "payload", context.payload(),
                        "metadata", context.metadata(),
                        "correlationId", context.executionContext().correlationId(),
                        "executionId", context.executionContext().executionId(),
                        "parentExecutionId", context.executionContext().parentExecutionId(),
                        "spanId", context.executionContext().spanId()
                );
            }

            if (value instanceof com.webempresarial.store.feature.automation.AutomationExecutionResult result) {
                return java.util.Map.of(
                        "success", result.success(),
                        "message", result.message()
                );
            }

            return value;
        }

        private String stacktrace(Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
        public void run(Object input, Runnable runnable) {
            get(input, () -> {
                runnable.run();
                return null;
            });
        }

        public <T> T get(Object input, Supplier<T> supplier) {
            return execute(input, childScope -> supplier.get());
        }

        public void runWithScope(Object input, Consumer<ExecutionScope> consumer) {
            execute(input, childScope -> {
                consumer.accept(childScope);
                return null;
            });
        }
        
        private String extractPayload(Object input) {
            AutomationContext context = findAutomationContext(input);

            if (context == null) {
                return null;
            }

            return serialize(context.payload());
        }

        private String extractMetadata(Object input) {
            AutomationContext context = findAutomationContext(input);

            if (context == null) {
                return null;
            }

            return serialize(context.metadata());
        }

        private AutomationContext findAutomationContext(Object value) {
            if (value == null) {
                return null;
            }

            if (value instanceof AutomationContext context) {
                return context;
            }

            if (value instanceof Object[] array) {
                for (Object item : array) {
                    AutomationContext found = findAutomationContext(item);
                    if (found != null) {
                        return found;
                    }
                }
            }

            return null;
        }


        
    }
}
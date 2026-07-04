package com.webempresarial.store.service;

import com.webempresarial.store.entity.ExecutionSpan;
import com.webempresarial.store.feature.runtime.ExecutionContext;
import com.webempresarial.store.repository.ExecutionSpanRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ExecutionSpanService {

    private final ExecutionSpanRepository repository;

    public ExecutionSpanService(ExecutionSpanRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(
            ExecutionContext context,
            String type,
            String name,
            String source,
            boolean success,
            String message,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            long durationMs
    ) {
        ExecutionSpan span = new ExecutionSpan();

        span.setCorrelationId(context.correlationId());
        span.setExecutionId(context.executionId());
        span.setParentExecutionId(context.parentExecutionId());
        span.setSpanId(context.spanId());

        span.setType(type);
        span.setName(name);
        span.setSource(source);

        span.setSuccess(success);
        span.setMessage(message);

        span.setStartedAt(startedAt);
        span.setFinishedAt(finishedAt);
        span.setDurationMs(durationMs);

        repository.save(span);
    }
}
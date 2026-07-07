package com.webempresarial.store.service;

import com.webempresarial.store.entity.ExecutionSpan;
import com.webempresarial.store.feature.runtime.ExecutionSpanRecord;
import com.webempresarial.store.repository.ExecutionSpanRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExecutionSpanService {

    private final ExecutionSpanRepository repository;

    public ExecutionSpanService(ExecutionSpanRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(ExecutionSpanRecord record) {
        ExecutionSpan span = new ExecutionSpan();

        span.setCorrelationId(record.context().correlationId());
        span.setExecutionId(record.context().executionId());
        span.setParentExecutionId(record.context().parentExecutionId());
        span.setSpanId(record.context().spanId());

        span.setType(record.type());
        span.setName(record.name());
        span.setSource(record.source());

        span.setSuccess(record.success());
        span.setMessage(record.message());

        span.setStartedAt(record.startedAt());
        span.setFinishedAt(record.finishedAt());
        span.setDurationMs(record.durationMs());

        span.setPayload(record.payload());
        span.setMetadata(record.metadata());
        span.setInput(record.input());
        span.setOutput(record.output());

        span.setExceptionType(record.exceptionType());
        span.setExceptionMessage(record.exceptionMessage());
        span.setStacktrace(record.stacktrace());

        repository.save(span);
    }
}
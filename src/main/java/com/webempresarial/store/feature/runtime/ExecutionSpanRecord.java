package com.webempresarial.store.feature.runtime;

import java.time.LocalDateTime;

public record ExecutionSpanRecord(
        ExecutionContext context,
        String type,
        String name,
        String source,
        boolean success,
        String message,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        long durationMs,
        String payload,
        String metadata,
        String input,
        String output,
        String exceptionType,
        String exceptionMessage,
        String stacktrace
) {}
package com.webempresarial.store.knowledge.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record KnowledgeApiErrorResponse(

        LocalDateTime timestamp,

        int status,

        String error,

        String message,

        String path,

        List<FieldViolation> violations
) {

    public KnowledgeApiErrorResponse {
        timestamp = timestamp != null
                ? timestamp
                : LocalDateTime.now();

        violations = violations == null
                ? List.of()
                : List.copyOf(violations);
    }

    public static KnowledgeApiErrorResponse of(
            int status,
            String error,
            String message,
            String path
    ) {
        return new KnowledgeApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                List.of()
        );
    }

    public static KnowledgeApiErrorResponse validation(
            int status,
            String error,
            String message,
            String path,
            List<FieldViolation> violations
    ) {
        return new KnowledgeApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                violations
        );
    }

    public record FieldViolation(
            String field,
            String message,
            Object rejectedValue
    ) {
    }
}
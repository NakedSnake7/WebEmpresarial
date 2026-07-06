package com.webempresarial.store.service;

import com.webempresarial.store.dto.platform.ExecutionNodeDetailDTO;
import com.webempresarial.store.repository.ExecutionSpanRepository;
import org.springframework.stereotype.Service;

@Service
public class ExecutionNodeInspectorService {

    private final ExecutionSpanRepository spanRepository;

    public ExecutionNodeInspectorService(ExecutionSpanRepository spanRepository) {
        this.spanRepository = spanRepository;
    }

    public ExecutionNodeDetailDTO findByExecutionId(String executionId) {
        return spanRepository.findByExecutionId(executionId)
                .map(span -> new ExecutionNodeDetailDTO(
                        span.getExecutionId(),
                        span.getParentExecutionId(),
                        span.getCorrelationId(),
                        span.getSpanId(),
                        span.getType(),
                        span.getName(),
                        span.getSource(),
                        span.isSuccess(),
                        span.getMessage(),
                        span.getStartedAt() != null ? span.getStartedAt().toString() : null,
                        span.getFinishedAt() != null ? span.getFinishedAt().toString() : null,
                        span.getDurationMs()
                ))
                .orElse(null);
    }
}
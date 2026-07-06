package com.webempresarial.store.repository;

import com.webempresarial.store.entity.ExecutionSpan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExecutionSpanRepository extends JpaRepository<ExecutionSpan, Long> {

    List<ExecutionSpan> findByCorrelationIdOrderByStartedAtDesc(String correlationId);
    
    Optional<ExecutionSpan> findByExecutionId(String executionId);
}
package com.webempresarial.store.repository;

import com.webempresarial.store.entity.ExecutionSpan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionSpanRepository extends JpaRepository<ExecutionSpan, Long> {

    List<ExecutionSpan> findByCorrelationIdOrderByStartedAtDesc(String correlationId);
}
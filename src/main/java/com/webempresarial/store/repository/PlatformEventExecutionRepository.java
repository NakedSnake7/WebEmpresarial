package com.webempresarial.store.repository;

import com.webempresarial.store.entity.PlatformEventExecution;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformEventExecutionRepository
        extends JpaRepository<PlatformEventExecution, Long> {
	
	List<PlatformEventExecution> findTop100ByOrderByOccurredAtDesc();
	
	List<PlatformEventExecution> findByCorrelationIdOrderByOccurredAtDesc(String correlationId);
}
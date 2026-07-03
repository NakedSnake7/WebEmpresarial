package com.webempresarial.store.repository;

import com.webempresarial.store.entity.PlatformEventListenerExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformEventListenerExecutionRepository
        extends JpaRepository<PlatformEventListenerExecution, Long> {
}
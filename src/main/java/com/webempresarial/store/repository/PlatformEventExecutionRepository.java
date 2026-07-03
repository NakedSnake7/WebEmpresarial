package com.webempresarial.store.repository;

import com.webempresarial.store.entity.PlatformEventExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformEventExecutionRepository
        extends JpaRepository<PlatformEventExecution, Long> {
}
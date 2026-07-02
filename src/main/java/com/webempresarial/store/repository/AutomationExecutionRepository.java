package com.webempresarial.store.repository;

import com.webempresarial.store.entity.AutomationExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomationExecutionRepository
        extends JpaRepository<AutomationExecution, Long> {

    List<AutomationExecution> findTop100ByOrderByStartedAtDesc();

    List<AutomationExecution> findByTriggerNameOrderByStartedAtDesc(
            String triggerName
    );
}
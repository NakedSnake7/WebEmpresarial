package com.webempresarial.store.repository;

import com.webempresarial.store.entity.AutomationExecutionAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomationExecutionActionRepository
        extends JpaRepository<AutomationExecutionAction, Long> {
}
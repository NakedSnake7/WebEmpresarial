package com.webempresarial.store.service;

import com.webempresarial.store.entity.AutomationExecution;
import com.webempresarial.store.repository.AutomationExecutionRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomationHistoryQueryService {

    private final AutomationExecutionRepository automationExecutionRepository;

    public AutomationHistoryQueryService(
            AutomationExecutionRepository automationExecutionRepository
    ) {
        this.automationExecutionRepository = automationExecutionRepository;
    }

    public List<AutomationExecution> latest() {
        return automationExecutionRepository.findTop100ByOrderByStartedAtDesc();
    }

    public AutomationExecution detail(Long id) {
        return automationExecutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ejecución no encontrada: " + id
                ));
    }
}
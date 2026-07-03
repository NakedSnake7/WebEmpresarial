package com.webempresarial.store.service;

import com.webempresarial.store.entity.PlatformEventExecution;
import com.webempresarial.store.entity.PlatformEventListenerExecution;
import com.webempresarial.store.feature.event.EventExecutionResult;
import com.webempresarial.store.feature.event.PlatformEventReport;
import com.webempresarial.store.repository.PlatformEventExecutionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PlatformEventHistoryService {

    private final PlatformEventExecutionRepository repository;

    public PlatformEventHistoryService(
            PlatformEventExecutionRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public void save(PlatformEventReport report) {

        if (report == null || report.listeners().isEmpty()) {
            return;
        }

        PlatformEventExecution execution = new PlatformEventExecution();

        execution.setEventName(report.event().name());
        execution.setSourceModule(report.event().sourceModule());
        execution.setOccurredAt(report.event().occurredAt());
        execution.setFinishedAt(LocalDateTime.now());
        execution.setSuccess(report.success());
        execution.setTotalListeners(report.listeners().size());

        long totalDuration = report.listeners()
                .stream()
                .mapToLong(EventExecutionResult::executionTimeMs)
                .sum();

        execution.setTotalDurationMs(totalDuration);

        for (EventExecutionResult result : report.listeners()) {
            PlatformEventListenerExecution listener =
                    new PlatformEventListenerExecution();

            listener.setListenerName(result.listener());
            listener.setSuccess(result.success());
            listener.setDurationMs(result.executionTimeMs());
            listener.setMessage(result.message());

            execution.addListener(listener);
        }

        repository.save(execution);
    }
}
package com.webempresarial.store.service;

import com.webempresarial.store.dto.platform.PlatformTimelineItemDTO;
import com.webempresarial.store.repository.AutomationExecutionRepository;
import com.webempresarial.store.repository.PlatformEventExecutionRepository;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PlatformTimelineService {

    private final AutomationExecutionRepository automationRepository;
    private final PlatformEventExecutionRepository eventRepository;

    public PlatformTimelineService(
            AutomationExecutionRepository automationRepository,
            PlatformEventExecutionRepository eventRepository
    ) {
        this.automationRepository = automationRepository;
        this.eventRepository = eventRepository;
    }

    public List<PlatformTimelineItemDTO> latest() {
        var automations = automationRepository.findTop100ByOrderByStartedAtDesc()
                .stream()
                .map(item -> new PlatformTimelineItemDTO(
                        "AUTOMATION",
                        item.getTriggerName(),
                        "AutomationEngine",
                        item.isSuccess() ? "SUCCESS" : "FAILED",
                        item.getTotalActions() + " acciones ejecutadas",
                        item.getStartedAt().toString(),
                        item.getTotalDurationMs(),
                        item.getCorrelationId(),
                        item.getExecutionId(),
                        item.getParentExecutionId(),
                        item.getSpanId()
                ));

        var events = eventRepository.findTop100ByOrderByOccurredAtDesc()
                .stream()
                .map(item -> new PlatformTimelineItemDTO(
                        "EVENT",
                        item.getEventName(),
                        item.getSourceModule(),
                        item.isSuccess() ? "SUCCESS" : "FAILED",
                        item.getTotalListeners() + " listeners ejecutados",
                        item.getOccurredAt().toString(),
                        item.getTotalDurationMs(),
                        item.getCorrelationId(),
                        item.getExecutionId(),
                        item.getParentExecutionId(),
                        item.getSpanId()
                ));

        return Stream.concat(automations, events)
                .sorted(Comparator.comparing(PlatformTimelineItemDTO::occurredAt).reversed())
                .limit(100)
                .toList();
    }
    public List<PlatformTimelineItemDTO> byCorrelationId(String correlationId) {
        var automations = automationRepository
                .findByCorrelationIdOrderByStartedAtDesc(correlationId)
                .stream()
                .map(item -> new PlatformTimelineItemDTO(
                        "AUTOMATION",
                        item.getTriggerName(),
                        "AutomationEngine",
                        item.isSuccess() ? "SUCCESS" : "FAILED",
                        item.getTotalActions() + " acciones ejecutadas",
                        item.getStartedAt().toString(),
                        item.getTotalDurationMs(),
                        item.getCorrelationId(),
                        item.getExecutionId(),
                        item.getParentExecutionId(),
                        item.getSpanId()
                ));

        var events = eventRepository
                .findByCorrelationIdOrderByOccurredAtDesc(correlationId)
                .stream()
                .map(item -> new PlatformTimelineItemDTO(
                        "EVENT",
                        item.getEventName(),
                        item.getSourceModule(),
                        item.isSuccess() ? "SUCCESS" : "FAILED",
                        item.getTotalListeners() + " listeners ejecutados",
                        item.getOccurredAt().toString(),
                        item.getTotalDurationMs(),
                        item.getCorrelationId(),
                        item.getExecutionId(),
                        item.getParentExecutionId(),
                        item.getSpanId()
                ));

        return Stream.concat(automations, events)
                .sorted(Comparator.comparing(PlatformTimelineItemDTO::occurredAt).reversed())
                .toList();
    }
}
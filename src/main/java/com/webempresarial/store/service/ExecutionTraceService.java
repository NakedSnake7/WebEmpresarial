package com.webempresarial.store.service;

import com.webempresarial.store.dto.platform.ExecutionTraceDTO;
import com.webempresarial.store.dto.platform.ExecutionTraceNodeDTO;
import com.webempresarial.store.repository.AutomationExecutionRepository;
import com.webempresarial.store.repository.PlatformEventExecutionRepository;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExecutionTraceService {

    private final AutomationExecutionRepository automationRepository;
    private final PlatformEventExecutionRepository eventRepository;

    public ExecutionTraceService(
            AutomationExecutionRepository automationRepository,
            PlatformEventExecutionRepository eventRepository
    ) {
        this.automationRepository = automationRepository;
        this.eventRepository = eventRepository;
    }

    public ExecutionTraceDTO build(String correlationId) {

        Map<String, ExecutionTraceNodeDTO> nodes = new LinkedHashMap<>();

        eventRepository.findByCorrelationIdOrderByOccurredAtDesc(correlationId)
                .forEach(event -> {
                    ExecutionTraceNodeDTO node = new ExecutionTraceNodeDTO();

                    node.setExecutionId(event.getExecutionId());
                    node.setParentExecutionId(event.getParentExecutionId());
                    node.setSpanId(event.getSpanId());
                    node.setType("EVENT");
                    node.setTitle(event.getEventName());
                    node.setSource(event.getSourceModule());
                    node.setStatus(event.isSuccess() ? "SUCCESS" : "FAILED");
                    node.setOccurredAt(event.getOccurredAt().toString());
                    node.setDurationMs(event.getTotalDurationMs());

                    nodes.put(node.getExecutionId(), node);
                });

        automationRepository.findByCorrelationIdOrderByStartedAtDesc(correlationId)
                .forEach(automation -> {
                    ExecutionTraceNodeDTO node = new ExecutionTraceNodeDTO();

                    node.setExecutionId(automation.getExecutionId());
                    node.setParentExecutionId(automation.getParentExecutionId());
                    node.setSpanId(automation.getSpanId());
                    node.setType("AUTOMATION");
                    node.setTitle(automation.getTriggerName());
                    node.setSource("AutomationEngine");
                    node.setStatus(automation.isSuccess() ? "SUCCESS" : "FAILED");
                    node.setOccurredAt(automation.getStartedAt().toString());
                    node.setDurationMs(automation.getTotalDurationMs());

                    nodes.put(node.getExecutionId(), node);
                });

        nodes.values().forEach(node -> {
            String parentId = node.getParentExecutionId();

            if (parentId != null && nodes.containsKey(parentId)) {
                nodes.get(parentId).addChild(node);
            }
        });

        List<ExecutionTraceNodeDTO> roots = nodes.values()
                .stream()
                .filter(node ->
                        node.getParentExecutionId() == null
                                || !nodes.containsKey(node.getParentExecutionId())
                )
                .sorted(Comparator.comparing(ExecutionTraceNodeDTO::getOccurredAt))
                .toList();

        return new ExecutionTraceDTO(correlationId, roots);
    }
}
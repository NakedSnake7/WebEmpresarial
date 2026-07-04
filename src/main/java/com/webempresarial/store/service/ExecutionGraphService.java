package com.webempresarial.store.service;

import com.webempresarial.store.dto.platform.ExecutionGraphDTO;
import com.webempresarial.store.dto.platform.ExecutionGraphEdgeDTO;
import com.webempresarial.store.dto.platform.ExecutionGraphNodeDTO;
import com.webempresarial.store.dto.platform.PositionedGraphDTO;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExecutionGraphService {

    private final ExecutionTraceService executionTraceService;
    private final GraphLayoutEngine graphLayoutEngine;

    public ExecutionGraphService(
            ExecutionTraceService executionTraceService,
            GraphLayoutEngine graphLayoutEngine
    ) {
        this.executionTraceService = executionTraceService;
        this.graphLayoutEngine = graphLayoutEngine;
    }

    public PositionedGraphDTO buildPositioned(String correlationId) {
        return graphLayoutEngine.layout(build(correlationId));
    }
    public ExecutionGraphDTO build(String correlationId) {
        var trace = executionTraceService.build(correlationId);

        List<ExecutionGraphNodeDTO> nodes = new ArrayList<>();
        List<ExecutionGraphEdgeDTO> edges = new ArrayList<>();

        trace.roots().forEach(root ->
                flatten(root, nodes, edges)
        );

        long totalDuration = nodes.stream()
                .mapToLong(ExecutionGraphNodeDTO::durationMs)
                .sum();

        int failed = (int) nodes.stream()
                .filter(node -> "FAILED".equals(node.status()))
                .count();

        int success = (int) nodes.stream()
                .filter(node -> "SUCCESS".equals(node.status()))
                .count();

        return new ExecutionGraphDTO(
                correlationId,
                totalDuration,
                nodes.size(),
                failed,
                success,
                nodes,
                edges
        );
    }

    private void flatten(
            com.webempresarial.store.dto.platform.ExecutionTraceNodeDTO node,
            List<ExecutionGraphNodeDTO> nodes,
            List<ExecutionGraphEdgeDTO> edges
    ) {
        nodes.add(new ExecutionGraphNodeDTO(
                node.getExecutionId(),
                node.getTitle(),
                node.getType(),
                node.getStatus(),
                node.getSource(),
                node.getDurationMs()
        ));

        node.getChildren().forEach(child -> {
            edges.add(new ExecutionGraphEdgeDTO(
                    node.getExecutionId(),
                    child.getExecutionId(),
                    "child"
            ));

            flatten(child, nodes, edges);
        });
    }
}
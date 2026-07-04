package com.webempresarial.store.service;

import com.webempresarial.store.dto.platform.ExecutionGraphDTO;
import com.webempresarial.store.dto.platform.PositionedGraphDTO;
import com.webempresarial.store.dto.platform.PositionedGraphNodeDTO;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class GraphLayoutEngine {

    private static final int X_GAP = 260;
    private static final int Y_GAP = 130;

    public PositionedGraphDTO layout(ExecutionGraphDTO graph) {

        Map<String, Integer> levels = new HashMap<>();

        graph.nodes().forEach(node ->
                levels.put(node.id(), 0)
        );

        graph.edges().forEach(edge -> {
            int parentLevel = levels.getOrDefault(edge.from(), 0);
            levels.put(edge.to(), Math.max(
                    levels.getOrDefault(edge.to(), 0),
                    parentLevel + 1
            ));
        });

        Map<Integer, Integer> levelCounts = new HashMap<>();

        var positioned = new ArrayList<PositionedGraphNodeDTO>();

        graph.nodes().forEach(node -> {
            int level = levels.getOrDefault(node.id(), 0);
            int index = levelCounts.getOrDefault(level, 0);

            levelCounts.put(level, index + 1);

            positioned.add(new PositionedGraphNodeDTO(
                    node.id(),
                    node.label(),
                    node.type(),
                    node.status(),
                    node.source(),
                    node.durationMs(),
                    80 + (level * X_GAP),
                    80 + (index * Y_GAP),
                    level
            ));
        });

        return new PositionedGraphDTO(
                graph.correlationId(),
                graph.totalDurationMs(),
                graph.totalNodes(),
                graph.failedNodes(),
                graph.successfulNodes(),
                positioned,
                graph.edges()
        );
    }
}
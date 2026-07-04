package com.webempresarial.store.controller.admin.api;

import com.webempresarial.store.dto.platform.ExecutionGraphDTO;
import com.webempresarial.store.dto.platform.PositionedGraphDTO;
import com.webempresarial.store.service.ExecutionGraphService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlatformGraphApiController {

    private final ExecutionGraphService executionGraphService;

    public PlatformGraphApiController(
            ExecutionGraphService executionGraphService
    ) {
        this.executionGraphService = executionGraphService;
    }

    @GetMapping("/api/admin/platform/graph/{correlationId}")
    public ExecutionGraphDTO graph(
            @PathVariable String correlationId
    ) {
        return executionGraphService.build(correlationId);
    }

    @GetMapping("/api/admin/platform/graph/{correlationId}/positioned")
    public PositionedGraphDTO positionedGraph(
            @PathVariable String correlationId
    ) {
        return executionGraphService.buildPositioned(correlationId);
    }
    
}
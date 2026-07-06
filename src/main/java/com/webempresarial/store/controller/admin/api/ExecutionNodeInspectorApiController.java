package com.webempresarial.store.controller.admin.api;

import com.webempresarial.store.dto.platform.ExecutionNodeDetailDTO;
import com.webempresarial.store.service.ExecutionNodeInspectorService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/platform/nodes")
public class ExecutionNodeInspectorApiController {

    private final ExecutionNodeInspectorService inspectorService;

    public ExecutionNodeInspectorApiController(
            ExecutionNodeInspectorService inspectorService
    ) {
        this.inspectorService = inspectorService;
    }

    @GetMapping("/{executionId}")
    public ExecutionNodeDetailDTO detail(
            @PathVariable String executionId
    ) {
        return inspectorService.findByExecutionId(executionId);
    }
}
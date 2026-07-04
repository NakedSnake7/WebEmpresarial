package com.webempresarial.store.controller.admin;

import com.webempresarial.store.service.ExecutionGraphService;
import com.webempresarial.store.service.ExecutionTraceService; 
import com.webempresarial.store.service.PlatformTimelineService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PlatformTimelineController {

    private final PlatformTimelineService timelineService;
    private final ExecutionTraceService executionTraceService;
    private final ExecutionGraphService executionGraphService;

    public PlatformTimelineController(
            PlatformTimelineService timelineService,
            ExecutionTraceService executionTraceService,
            ExecutionGraphService executionGraphService
    ) {
        this.timelineService = timelineService;
        this.executionTraceService = executionTraceService;
        this.executionGraphService = executionGraphService;
    }

    @GetMapping("/admin/platform/timeline/{correlationId}")
    public String timeline(
            @PathVariable String correlationId,
            Model model
    ) {
        model.addAttribute("correlationId", correlationId);
        model.addAttribute("timeline", timelineService.byCorrelationId(correlationId));

        return "admin/platform/timeline-detail";
    }
    
    @GetMapping("/admin/platform/trace/{correlationId}")
    public String trace(
            @PathVariable String correlationId,
            Model model
    ) {
        model.addAttribute("trace", executionTraceService.build(correlationId));
        return "admin/platform/trace-detail";
    }
    @GetMapping("/admin/platform/graph/{correlationId}")
    public String graph(
            @PathVariable String correlationId,
            Model model
    ) {
        model.addAttribute("graph", executionGraphService.buildPositioned(correlationId));
        return "admin/platform/graph-detail";
    }
}
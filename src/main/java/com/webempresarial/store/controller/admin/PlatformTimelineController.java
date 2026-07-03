package com.webempresarial.store.controller.admin;

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

    public PlatformTimelineController(
            PlatformTimelineService timelineService,
            ExecutionTraceService executionTraceService
    ) {
        this.timelineService = timelineService;
        this.executionTraceService = executionTraceService;
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
}
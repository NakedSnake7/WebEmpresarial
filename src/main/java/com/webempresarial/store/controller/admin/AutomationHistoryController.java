package com.webempresarial.store.controller.admin;

import com.webempresarial.store.service.AutomationHistoryQueryService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AutomationHistoryController {

    private final AutomationHistoryQueryService queryService;

    public AutomationHistoryController(
            AutomationHistoryQueryService queryService
    ) {
        this.queryService = queryService;
    }

    @GetMapping("/admin/automations/history")
    public String history(Model model) {
        model.addAttribute("executions", queryService.latest());
        return "admin/automations/history";
    }

    @GetMapping("/admin/automations/history/{id}")
    public String detail(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute("execution", queryService.detail(id));
        return "admin/automations/history-detail";
    }
}
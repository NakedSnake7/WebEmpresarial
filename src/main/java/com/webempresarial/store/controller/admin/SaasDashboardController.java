package com.webempresarial.store.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webempresarial.store.service.SaasMetricsService;

@Controller
@RequestMapping("/admin/saas")
public class SaasDashboardController {

    private final SaasMetricsService saasMetricsService;

    public SaasDashboardController(
            SaasMetricsService saasMetricsService
    ) {
        this.saasMetricsService = saasMetricsService;
    }

    @GetMapping
    public String dashboard(Model model) {

        model.addAttribute(
                "metrics",
                saasMetricsService.getMetrics()
        );

        return "admin/saas/dashboard";
    }
}
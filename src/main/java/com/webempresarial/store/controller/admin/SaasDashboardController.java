package com.webempresarial.store.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webempresarial.store.service.FeatureUsageMetricsService;
import com.webempresarial.store.service.SaasMetricsService;

@Controller
@RequestMapping("/admin/saas")
public class SaasDashboardController {

    private final SaasMetricsService saasMetricsService;
    private final FeatureUsageMetricsService featureUsageMetricsService;

    public SaasDashboardController(
            SaasMetricsService saasMetricsService,
            FeatureUsageMetricsService featureUsageMetricsService
    ) {
        this.saasMetricsService = saasMetricsService;
        this.featureUsageMetricsService = featureUsageMetricsService;
    }

    @GetMapping
    public String dashboard(Model model) {

        model.addAttribute(
                "metrics",
                saasMetricsService.getMetrics()
        );

        model.addAttribute(
                "featureUsage",
                featureUsageMetricsService.getUsageLast30Days()
        );

        model.addAttribute(
                "topStores",
                featureUsageMetricsService.getTopStoresLast30Days()
        );

        return "admin/saas/dashboard";
    }
}
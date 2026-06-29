package com.webempresarial.store.controller.admin;

import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webempresarial.store.service.FeatureUsageMetricsService;
import com.webempresarial.store.service.HealthScoreService;
import com.webempresarial.store.service.RecommendationEngineService;
import com.webempresarial.store.service.SaasMetricSnapshotService;
import com.webempresarial.store.service.SaasMetricsService;
import com.webempresarial.store.service.ExecutiveMetricsService;
import com.webempresarial.store.service.ModuleViewService;
@Controller
@RequestMapping("/admin/saas")
public class SaasDashboardController {

    private final SaasMetricsService saasMetricsService;
    private final FeatureUsageMetricsService featureUsageMetricsService;
    private final SaasMetricSnapshotService snapshotService;
    private final ExecutiveMetricsService executiveMetricsService;
    private final RecommendationEngineService recommendationEngineService;
    private final HealthScoreService healthScoreService;
    private final ModuleViewService moduleViewService;

    public SaasDashboardController(
            SaasMetricsService saasMetricsService,
            FeatureUsageMetricsService featureUsageMetricsService,
            SaasMetricSnapshotService snapshotService,
            ExecutiveMetricsService executiveMetricsService,
            RecommendationEngineService recommendationEngineService,
            HealthScoreService healthScoreService,
            ModuleViewService moduleViewService
    ) {
        this.saasMetricsService = saasMetricsService;
        this.featureUsageMetricsService = featureUsageMetricsService;
        this.snapshotService = snapshotService;
        this.executiveMetricsService = executiveMetricsService;
        this.recommendationEngineService = recommendationEngineService;
        this.healthScoreService = healthScoreService;
        this.moduleViewService = moduleViewService;
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
        
        model.addAttribute(
        		"mrrSnapshots", snapshotService.getMrrLast30Days()
        );
        
        model.addAttribute("executive", executiveMetricsService.getMetrics());
        
        model.addAttribute(
                "recommendations",
                recommendationEngineService.getRecommendations()
        );
        model.addAttribute(
                "storeHealth",
                healthScoreService.calculateAllStores()
        );
        
        model.addAttribute(
                "modules",
                moduleViewService.modules()
        );

        return "admin/saas/dashboard";
    }
}
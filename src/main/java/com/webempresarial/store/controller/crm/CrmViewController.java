package com.webempresarial.store.controller.crm;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.FeatureUsageService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CrmViewController {

    private final StoreContextService storeContextService;
    private final FeatureAccessService featureAccessService;
    private final FeatureUsageService featureUsageService;

    public CrmViewController(
            StoreContextService storeContextService,
            FeatureAccessService featureAccessService,
            FeatureUsageService featureUsageService
    ) {
        this.storeContextService = storeContextService;
        this.featureAccessService = featureAccessService;
        this.featureUsageService = featureUsageService;
    }

    @GetMapping("/crm/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(store, Feature.CRM);
        featureUsageService.track(store, Feature.CRM, "crm_dashboard");

        model.addAttribute("crmPage", true);
        model.addAttribute("title", "CRM Dashboard | WebEmpresarial");

        return "crm/dashboard";
    }

    @GetMapping("/crm/pipeline")
    public String pipeline(Model model, HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(store, Feature.PIPELINE);
        featureUsageService.track(store, Feature.PIPELINE, "crm_pipeline");

        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Pipeline Comercial | WebEmpresarial");

        return "crm/pipeline";
    }

    @GetMapping("/crm/tasks")
    public String tasks(Model model, HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(store, Feature.TASKS);
        featureUsageService.track(store, Feature.TASKS, "crm_tasks");

        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Tareas Comerciales | WebEmpresarial");

        return "crm/tasks";
    }

    @GetMapping("/crm/reports")
    public String reports(Model model, HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(store, Feature.ANALYTICS);
        featureUsageService.track(store, Feature.ANALYTICS, "crm_reports");

        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Reportes CRM | WebEmpresarial");

        return "crm/reports";
    }
}
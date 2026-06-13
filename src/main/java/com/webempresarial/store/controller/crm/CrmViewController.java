package com.webempresarial.store.controller.crm;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CrmViewController {

    private final StoreContextService storeContextService;
    private final FeatureAccessService featureAccessService;

    public CrmViewController(
            StoreContextService storeContextService,
            FeatureAccessService featureAccessService
    ) {
        this.storeContextService = storeContextService;
        this.featureAccessService = featureAccessService;
    }

    @GetMapping("/crm/dashboard")
    public String dashboard(
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(
                store,
                Feature.CRM
        );

        model.addAttribute("crmPage", true);
        model.addAttribute("title", "CRM Dashboard | WebEmpresarial");

        return "crm/dashboard";
    }

    @GetMapping("/crm/pipeline")
    public String pipeline(
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(
                store,
                Feature.PIPELINE
        );

        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Pipeline Comercial | WebEmpresarial");

        return "crm/pipeline";
    }

    @GetMapping("/crm/tasks")
    public String tasks(
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(
                store,
                Feature.TASKS
        );

        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Tareas Comerciales | WebEmpresarial");

        return "crm/tasks";
    }

    @GetMapping("/crm/reports")
    public String reports(
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(
                store,
                Feature.ANALYTICS
        );

        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Reportes CRM | WebEmpresarial");

        return "crm/reports";
    }
}
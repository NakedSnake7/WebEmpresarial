package com.webempresarial.store.controller.admin.store.crm;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.crm.LeadBudgetRangeService;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/store/crm")
public class StoreCrmSettingsController {

    private final StoreContextService storeContextService;
    private final LeadBudgetRangeService budgetRangeService;
    private final FeatureAccessService featureAccessService;

    public StoreCrmSettingsController(
            StoreContextService storeContextService,
            LeadBudgetRangeService budgetRangeService,
            FeatureAccessService featureAccessService
    ) {
        this.storeContextService = storeContextService;
        this.budgetRangeService = budgetRangeService;
        this.featureAccessService = featureAccessService;
    }

    @GetMapping
    public String index(
            Model model,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithFeature(
                request,
                Feature.CRM
        );

        model.addAttribute("store", store);

        return "admin/store/crm/index";
    }

    @GetMapping("/budget-ranges")
    public String budgetRanges(
            Model model,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithFeature(
                request,
                Feature.CRM
        );

        model.addAttribute("store", store);
        model.addAttribute(
                "budgetRanges",
                budgetRangeService.getRanges(store)
        );

        return "admin/store/crm/budget-ranges";
    }

    @PostMapping("/budget-ranges")
    public String createBudgetRange(
            @RequestParam String code,
            @RequestParam String label,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) BigDecimal estimatedAmount,
            @RequestParam(required = false) Integer scoreWeight,
            @RequestParam(required = false) Integer sortOrder,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithFeature(
                request,
                Feature.CRM
        );

        budgetRangeService.create(
                store,
                code,
                label,
                minAmount,
                maxAmount,
                estimatedAmount,
                scoreWeight,
                sortOrder
        );

        return "redirect:/admin/store/crm/budget-ranges?budgetCreated";
    }

    @PostMapping("/budget-ranges/{id}/update")
    public String updateBudgetRange(
            @PathVariable Long id,
            @RequestParam String label,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) BigDecimal estimatedAmount,
            @RequestParam(required = false) Integer scoreWeight,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(defaultValue = "false") boolean active,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithFeature(
                request,
                Feature.CRM
        );

        budgetRangeService.update(
                id,
                store,
                label,
                minAmount,
                maxAmount,
                estimatedAmount,
                scoreWeight,
                sortOrder,
                active
        );

        return "redirect:/admin/store/crm/budget-ranges?budgetUpdated";
    }

    @PostMapping("/budget-ranges/{id}/delete")
    public String deleteBudgetRange(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithFeature(
                request,
                Feature.CRM
        );

        budgetRangeService.delete(
                id,
                store
        );

        return "redirect:/admin/store/crm/budget-ranges?budgetDeleted";
    }

    @GetMapping("/lead-sources")
    public String leadSources(
            HttpServletRequest request
    ) {
        currentStoreWithFeature(
                request,
                Feature.CRM
        );

        return "admin/store/crm/lead-sources";
    }

    @GetMapping("/pipeline")
    public String pipeline(
            HttpServletRequest request
    ) {
        currentStoreWithFeature(
                request,
                Feature.PIPELINE
        );

        return "admin/store/crm/pipeline";
    }

    @GetMapping("/scoring")
    public String scoring(
            HttpServletRequest request
    ) {
        currentStoreWithFeature(
                request,
                Feature.CRM
        );

        return "admin/store/crm/scoring";
    }

    @GetMapping("/duplicate")
    public String duplicate(
            HttpServletRequest request
    ) {
        currentStoreWithFeature(
                request,
                Feature.CRM
        );

        return "admin/store/crm/duplicate";
    }

    @GetMapping("/merge")
    public String merge(
            HttpServletRequest request
    ) {
        currentStoreWithFeature(
                request,
                Feature.CRM
        );

        return "admin/store/crm/merge";
    }

    @GetMapping("/automation")
    public String automation(
            HttpServletRequest request
    ) {
        currentStoreWithFeature(
                request,
                Feature.AUTOMATIONS
        );

        return "admin/store/crm/automation";
    }

    @GetMapping("/ai")
    public String ai(
            HttpServletRequest request
    ) {
        /*
         * Placeholder only.
         *
         * AI CRM does not have a product entitlement yet.
         * Until real functionality exists, it inherits the
         * base CRM feature instead of inventing AI_CRM.
         */
        currentStoreWithFeature(
                request,
                Feature.CRM
        );

        return "admin/store/crm/ai";
    }

    private Store currentStoreWithFeature(
            HttpServletRequest request,
            Feature feature
    ) {
        Store store =
                storeContextService.getCurrentStore(
                        request
                );

        featureAccessService.requireFeature(
                store,
                feature
        );

        return store;
    }
}

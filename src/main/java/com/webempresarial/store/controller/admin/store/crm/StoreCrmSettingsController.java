package com.webempresarial.store.controller.admin.store.crm;

import com.webempresarial.store.model.Store;
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

    public StoreCrmSettingsController(
            StoreContextService storeContextService,
            LeadBudgetRangeService budgetRangeService
    ) {
        this.storeContextService = storeContextService;
        this.budgetRangeService = budgetRangeService;
    }

    @GetMapping
    public String index(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        model.addAttribute("store", store);

        return "admin/store/crm/index";
    }

    @GetMapping("/budget-ranges")
    public String budgetRanges(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

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
        Store store = storeContextService.getCurrentStore(request);

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
        Store store = storeContextService.getCurrentStore(request);

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
        Store store = storeContextService.getCurrentStore(request);

        budgetRangeService.delete(id, store);

        return "redirect:/admin/store/crm/budget-ranges?budgetDeleted";
    }

    @GetMapping("/lead-sources")
    public String leadSources() {
        return "admin/store/crm/lead-sources";
    }

    @GetMapping("/pipeline")
    public String pipeline() {
        return "admin/store/crm/pipeline";
    }

    @GetMapping("/scoring")
    public String scoring() {
        return "admin/store/crm/scoring";
    }

    @GetMapping("/duplicate")
    public String duplicate() {
        return "admin/store/crm/duplicate";
    }

    @GetMapping("/merge")
    public String merge() {
        return "admin/store/crm/merge";
    }

    @GetMapping("/automation")
    public String automation() {
        return "admin/store/crm/automation";
    }

    @GetMapping("/ai")
    public String ai() {
        return "admin/store/crm/ai";
    }
}
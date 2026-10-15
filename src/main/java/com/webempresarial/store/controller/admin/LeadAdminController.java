package com.webempresarial.store.controller.admin;

import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.LeadStatus;
import com.webempresarial.store.model.Store;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/leads")
public class LeadAdminController {

    private final LeadRepository leadRepository;
    private final StoreContextService storeContextService;
    private final FeatureAccessService featureAccessService;

    public LeadAdminController(
            LeadRepository leadRepository,
            StoreContextService storeContextService,
            FeatureAccessService featureAccessService
    ) {
        this.leadRepository = leadRepository;
        this.storeContextService = storeContextService;
        this.featureAccessService = featureAccessService;
    }

    @GetMapping
    public String index(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(store, Feature.LEADS);

        model.addAttribute(
                "leads",
                leadRepository.findByStoreIdAndMergedFalseOrderByCreatedAtDesc(store.getId())
        );

        model.addAttribute("statuses", LeadStatus.values());

        return "admin/leads/index";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam LeadStatus status,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(store, Feature.LEADS);

        var lead = leadRepository.findByIdAndStoreId(id, store.getId())
                .orElseThrow();

        lead.setStatus(status);
        leadRepository.save(lead);

        return "redirect:/admin/leads";
    }
}
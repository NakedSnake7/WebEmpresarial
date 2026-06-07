package com.webempresarial.store.controller;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.repository.StoreRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/admin/store/settings")
public class StoreSettingsController {

    private final StoreContextService storeContextService;
    private final StoreRepository storeRepository;

    public StoreSettingsController(
            StoreContextService storeContextService,
            StoreRepository storeRepository
    ) {
        this.storeContextService = storeContextService;
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public String settings(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        model.addAttribute("store", store);

        return "admin/stores/settings";
    }

    @CacheEvict(value = "storesByDomain", allEntries = true)
    @PostMapping
    public String saveSettings(
            @ModelAttribute Store form,
            HttpServletRequest request
    ) {
        Store currentStore = storeContextService.getCurrentStore(request);

        Store store = storeRepository.findById(currentStore.getId())
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        store.setLogoUrl(form.getLogoUrl());
        store.setCompanyEmail(form.getCompanyEmail());
        store.setCompanyPhone(form.getCompanyPhone());
        store.setCompanyAddress(form.getCompanyAddress());
        store.setCompanyWebsite(form.getCompanyWebsite());
        store.setContactName(form.getContactName());
        store.setCurrency(form.getCurrency());
        store.setProposalFooter(form.getProposalFooter());

        storeRepository.save(store);

        return "redirect:/admin/store/settings?success";
    }
}
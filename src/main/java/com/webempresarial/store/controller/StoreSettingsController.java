package com.webempresarial.store.controller;

import com.webempresarial.store.dto.CloudinaryUploadResult;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.repository.StoreRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/admin/store/settings")
public class StoreSettingsController {

    private final StoreContextService storeContextService;
    private final StoreRepository storeRepository;
    private final CloudinaryService cloudinaryService;

    public StoreSettingsController(
            StoreContextService storeContextService,
            StoreRepository storeRepository,
            CloudinaryService cloudinaryService
    ) {
        this.storeContextService = storeContextService;
        this.storeRepository = storeRepository;
        this.cloudinaryService = cloudinaryService;
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
        store.setFaviconUrl(form.getFaviconUrl());
        store.setPrimaryColor(form.getPrimaryColor());
        store.setSecondaryColor(form.getSecondaryColor());
        store.setAccentColor(form.getAccentColor());
        store.setFontFamily(form.getFontFamily());
        store.setHeroImageUrl(form.getHeroImageUrl());
        store.setSlogan(form.getSlogan());

        storeRepository.save(store);

        return "redirect:/admin/store/settings?success";
    }
    
    @CacheEvict(value = "storesByDomain", allEntries = true)
    @PostMapping("/logo")
    public String uploadLogo(
            @RequestParam("logoFile") MultipartFile logoFile,
            HttpServletRequest request
    ) {
        if (logoFile == null || logoFile.isEmpty()) {
            return "redirect:/admin/store/settings?logoError";
        }

        try {
            Store currentStore = storeContextService.getCurrentStore(request);

            Store store = storeRepository.findById(currentStore.getId())
                    .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

            if (store.getLogoUrl() != null &&
                    !store.getLogoUrl().isBlank()) {

                String publicId =
                        cloudinaryService.extraerPublicIdDesdeUrl(
                                store.getLogoUrl()
                        );

                if (publicId != null) {
                    cloudinaryService.eliminarImagen(publicId);
                }
            }

            CloudinaryUploadResult result =
                    cloudinaryService.subirLogoTienda(
                            logoFile,
                            store.getId()
                    );

            store.setLogoUrl(result.getSecureUrl());

            storeRepository.save(store);

            return "redirect:/admin/store/settings?logoSuccess";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/store/settings?logoError";
        }
    }
    
    @CacheEvict(value = "storesByDomain", allEntries = true)
    @PostMapping("/logo/delete")
    public String deleteLogo(HttpServletRequest request) {

        try {
            Store currentStore = storeContextService.getCurrentStore(request);

            Store store = storeRepository.findById(currentStore.getId())
                    .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

            if (store.getLogoUrl() != null && !store.getLogoUrl().isBlank()) {

                String publicId = cloudinaryService.extraerPublicIdDesdeUrl(
                        store.getLogoUrl()
                );

                if (publicId != null) {
                    cloudinaryService.eliminarImagen(publicId);
                }
            }

            store.setLogoUrl(null);

            storeRepository.save(store);

            return "redirect:/admin/store/settings?logoDeleted";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/store/settings?logoError";
        }
    }
    
}
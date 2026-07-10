package com.webempresarial.store.controller;

import com.webempresarial.store.dto.CloudinaryUploadResult; 
import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;


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
    private final StoreSettingsService storeSettingsService;
    private final CloudinaryService cloudinaryService;
    private final FeatureAccessService features;

    public StoreSettingsController(
            StoreContextService storeContextService,
            StoreSettingsService storeSettingsService,
            CloudinaryService cloudinaryService,
            FeatureAccessService features
    ) {
        this.storeContextService = storeContextService;
        this.storeSettingsService = storeSettingsService;
        this.cloudinaryService = cloudinaryService;
        this.features = features;
    }

    @GetMapping
    public String settings(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        StoreSettings settings = storeSettingsService.getOrCreate(store);

        model.addAttribute("store", store);
        model.addAttribute("settings", settings);
        model.addAttribute("features", features);

        return "admin/store/settings";
    }

    @CacheEvict(value = "storesByDomain", allEntries = true)
    @PostMapping
    public String saveSettings(
            @ModelAttribute StoreSettings form,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        StoreSettings settings = storeSettingsService.getOrCreate(store);

        settings.setCompanyEmail(form.getCompanyEmail());
        settings.setCompanyPhone(form.getCompanyPhone());
        settings.setCompanyAddress(form.getCompanyAddress());
        settings.setCompanyWebsite(form.getCompanyWebsite());
        settings.setContactName(form.getContactName());
        settings.setCurrency(form.getCurrency());
        settings.setProposalFooter(form.getProposalFooter());

        settings.setFaviconUrl(form.getFaviconUrl());
        settings.setPrimaryColor(form.getPrimaryColor());
        settings.setSecondaryColor(form.getSecondaryColor());
        settings.setAccentColor(form.getAccentColor());
        settings.setFontFamily(form.getFontFamily());
        settings.setHeroImageUrl(form.getHeroImageUrl());
        settings.setSlogan(form.getSlogan());
        
        settings.setCustomCss(form.getCustomCss());
        settings.setCustomJs(form.getCustomJs());
        
        settings.setGoogleAnalyticsId(form.getGoogleAnalyticsId());
        settings.setMetaPixelId(form.getMetaPixelId());
        settings.setTiktokPixelId(form.getTiktokPixelId());
        settings.setHotjarId(form.getHotjarId());

        storeSettingsService.save(settings);

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
            Store store = storeContextService.getCurrentStore(request);
            StoreSettings settings = storeSettingsService.getOrCreate(store);

            if (settings.getLogoUrl() != null &&
                    !settings.getLogoUrl().isBlank()) {

                String publicId =
                        cloudinaryService.extraerPublicIdDesdeUrl(
                                settings.getLogoUrl()
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

            settings.setLogoUrl(result.getSecureUrl());

            storeSettingsService.save(settings);

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
            Store store = storeContextService.getCurrentStore(request);
            StoreSettings settings = storeSettingsService.getOrCreate(store);

            if (settings.getLogoUrl() != null &&
                    !settings.getLogoUrl().isBlank()) {

                String publicId =
                        cloudinaryService.extraerPublicIdDesdeUrl(
                                settings.getLogoUrl()
                        );

                if (publicId != null) {
                    cloudinaryService.eliminarImagen(publicId);
                }
            }

            settings.setLogoUrl(null);

            storeSettingsService.save(settings);

            return "redirect:/admin/store/settings?logoDeleted";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/store/settings?logoError";
        }
    }
}
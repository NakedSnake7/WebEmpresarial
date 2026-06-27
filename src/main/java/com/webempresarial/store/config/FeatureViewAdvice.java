package com.webempresarial.store.config;

import com.webempresarial.store.dto.feature.FeatureCardDTO;
import com.webempresarial.store.dto.feature.FeatureSectionDTO;
import com.webempresarial.store.feature.FeatureViewService;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class FeatureViewAdvice {

    private final StoreContextService storeContextService;
    private final FeatureViewService featureViewService;

    public FeatureViewAdvice(
            StoreContextService storeContextService,
            FeatureViewService featureViewService
    ) {
        this.storeContextService = storeContextService;
        this.featureViewService = featureViewService;
    }

    @ModelAttribute("sidebarFeatures")
    public List<FeatureCardDTO> sidebarFeatures(
            HttpServletRequest request
    ) {

        try {

            Store store =
                    storeContextService.getCurrentStore(request);

            if (store == null) {
                return List.of();
            }

            return featureViewService.sidebar(store);

        } catch (Exception e) {

            return List.of();

        }

    }

    @ModelAttribute("availableFeatures")
    public List<FeatureCardDTO> availableFeatures(
            HttpServletRequest request
    ) {

        try {

            Store store =
                    storeContextService.getCurrentStore(request);

            if (store == null) {
                return List.of();
            }

            return featureViewService.available(store);

        } catch (Exception e) {

            return List.of();

        }

    }

    @ModelAttribute("lockedFeatures")
    public List<FeatureCardDTO> lockedFeatures(
            HttpServletRequest request
    ) {
        try {
            Store store = storeContextService.getCurrentStore(request);

            if (store == null) {
                return List.of();
            }

            return featureViewService.locked(store);

        } catch (Exception e) {
            return List.of();
        }
    }
    @ModelAttribute("sidebarSections")
    public List<FeatureSectionDTO> sidebarSections(
            HttpServletRequest request
    ) {
        try {
            Store store = storeContextService.getCurrentStore(request);

            if (store == null) {
                return List.of();
            }

            return featureViewService.sidebarSections(store);

        } catch (Exception e) {
            return List.of();
        }
    }

}
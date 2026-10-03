package com.webempresarial.store.config;

import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class StoreViewAdvice {

    private final StoreContextService storeContextService;
    private final StoreSettingsService storeSettingsService;
    private final FeatureAccessService featureAccessService;

    public StoreViewAdvice(
            StoreContextService storeContextService,
            StoreSettingsService storeSettingsService,
            FeatureAccessService featureAccessService
    ) {
        this.storeContextService = storeContextService;
        this.storeSettingsService = storeSettingsService;
        this.featureAccessService = featureAccessService;
    }

    @ModelAttribute("store")
    public Store store(HttpServletRequest request) {
        try {
            return storeContextService.getCurrentStore(request);
        } catch (Exception e) {
            return null;
        }
    }

    @ModelAttribute("settings")
    public StoreSettings settings(HttpServletRequest request) {
        try {
            Store store =
                    storeContextService.getCurrentStore(request);

            if (store == null) {
                return null;
            }

            return storeSettingsService.getOrCreate(store);

        } catch (Exception e) {
            return null;
        }
    }

    @ModelAttribute("features")
    public FeatureAccessService features() {
        return featureAccessService;
    }
}
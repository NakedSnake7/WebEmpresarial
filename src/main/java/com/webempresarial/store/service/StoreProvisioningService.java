package com.webempresarial.store.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.ThemeType;

@Service
public class StoreProvisioningService {

    private final SubscriptionService subscriptionService;
    private final StoreDefaultDataService storeDefaultDataService;
    private final StoreSettingsService storeSettingsService;

    public StoreProvisioningService(
            SubscriptionService subscriptionService,
            StoreDefaultDataService storeDefaultDataService,
            StoreSettingsService storeSettingsService
    ) {
        this.subscriptionService = subscriptionService;
        this.storeDefaultDataService = storeDefaultDataService;
        this.storeSettingsService = storeSettingsService;
    }

    @Transactional
    public void provision(
            Store store,
            String subscriptionType
    ) {

        applyDefaultBranding(store);

        createSubscription(store, subscriptionType);

        storeSettingsService.createDefaults(store);

        storeDefaultDataService.createDefaults(store);

        // Próximos pasos:
        // createDefaultStoreAdmin(store);
        // createDefaultCategories(store);
        // createDefaultCrmSettings(store);
        // sendWelcomeEmail(store);
    }

    private void applyDefaultBranding(Store store) {

        if (store.getCurrency() == null || store.getCurrency().isBlank()) {
            store.setCurrency("MXN");
        }

        if (store.getPrimaryColor() == null || store.getPrimaryColor().isBlank()) {
            store.setPrimaryColor("#111827");
        }

        if (store.getSecondaryColor() == null || store.getSecondaryColor().isBlank()) {
            store.setSecondaryColor("#6B7280");
        }

        if (store.getAccentColor() == null || store.getAccentColor().isBlank()) {
            store.setAccentColor("#2563EB");
        }

        if (store.getFontFamily() == null || store.getFontFamily().isBlank()) {
            store.setFontFamily("Inter");
        }
        
        if (store.getThemeType() == null) {
            store.setThemeType(ThemeType.BASIC);
        }

        if (store.getTheme() == null || store.getTheme().isBlank()) {

            switch (store.getThemeType()) {
                case BASIC -> store.setTheme("basic");
                case PRO -> store.setTheme("pro");
                case PREMIUM -> store.setTheme("premium");
                case CUSTOM -> store.setTheme("WebEmpresarial");
            }
        }
        
        if (store.getSlogan() == null || store.getSlogan().isBlank()) {
            store.setSlogan("Tu tienda online lista para vender");
        }

        if (store.getProposalFooter() == null || store.getProposalFooter().isBlank()) {
            store.setProposalFooter("""
                    50% anticipo
                    50% contra entrega
                    Vigencia de propuesta: 15 días
                    """);
        }
    }

    private void createSubscription(
            Store store,
            String subscriptionType
    ) {

        if (subscriptionType == null || subscriptionType.isBlank()) {
            subscriptionType = "INTERNAL";
        }

        switch (subscriptionType.toUpperCase()) {

            case "TRIAL" ->
                    subscriptionService.createTrial(
                            store,
                            store.getPlan()
                    );

            case "PAID" ->
                    subscriptionService.createTrial(
                            store,
                            store.getPlan()
                    );

            case "INTERNAL" ->
                    subscriptionService.createInternalSubscription(
                            store,
                            store.getPlan()
                    );

            default ->
                    subscriptionService.createInternalSubscription(
                            store,
                            store.getPlan()
                    );
        }
    }
}
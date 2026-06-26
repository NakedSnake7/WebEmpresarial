package com.webempresarial.store.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreSettingsRepository;

@Service
public class StoreSettingsService {

    private final StoreSettingsRepository storeSettingsRepository;

    public StoreSettingsService(StoreSettingsRepository storeSettingsRepository) {
        this.storeSettingsRepository = storeSettingsRepository;
    }

    @Transactional
    public StoreSettings createDefaults(Store store) {

        return storeSettingsRepository.findByStoreId(store.getId())
                .orElseGet(() -> {
                    StoreSettings settings = new StoreSettings();

                    settings.setStore(store);

                    settings.setLogoUrl(store.getLogoUrl());
                    settings.setFaviconUrl(store.getFaviconUrl());

                    settings.setPrimaryColor(store.getPrimaryColor());
                    settings.setSecondaryColor(store.getSecondaryColor());
                    settings.setAccentColor(store.getAccentColor());

                    settings.setFontFamily(store.getFontFamily());
                    settings.setHeroImageUrl(store.getHeroImageUrl());
                    settings.setSlogan(store.getSlogan());

                    settings.setCompanyEmail(store.getCompanyEmail());
                    settings.setCompanyPhone(store.getCompanyPhone());
                    settings.setCompanyAddress(store.getCompanyAddress());
                    settings.setCompanyWebsite(store.getCompanyWebsite());
                    settings.setContactName(store.getContactName());
                    
                    settings.setCustomCss("");
                    settings.setCustomJs("");

                    settings.setCurrency(store.getCurrency());
                    settings.setProposalFooter(store.getProposalFooter());

                    return storeSettingsRepository.save(settings);
                });
    }
    
    @Transactional
    public StoreSettings getOrCreate(Store store) {
        return storeSettingsRepository.findByStoreId(store.getId())
                .orElseGet(() -> createDefaults(store));
    }

    @Transactional
    public StoreSettings save(StoreSettings settings) {
        return storeSettingsRepository.save(settings);
    }
}
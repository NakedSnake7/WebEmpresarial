package com.webempresarial.store.service;

import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StoreSettingsRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class StoreSettingsServiceIntegrationTest {

    @Autowired
    private StoreSettingsService storeSettingsService;

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @Autowired
    private StoreRepository storeRepository;

    /*
     * =========================================================
     * CREATE DEFAULTS
     * =========================================================
     */

    @Test
    void shouldCreateDefaultSettingsFromStore() {

        Store store =
                createStore(
                        "Settings Defaults",
                        "settings-defaults"
                );

        store.setLogoUrl(
                "https://cdn.test/default-logo.png"
        );

        store.setFaviconUrl(
                "https://cdn.test/default-favicon.png"
        );

        store.setPrimaryColor("#123456");
        store.setSecondaryColor("#654321");
        store.setAccentColor("#ABCDEF");

        store.setFontFamily("Montserrat");

        store.setHeroImageUrl(
                "https://cdn.test/default-hero.jpg"
        );

        store.setSlogan(
                "Construimos el futuro"
        );

        store.setCompanyEmail(
                "ventas@settings-defaults.test"
        );

        store.setCompanyPhone(
                "+52 222 123 4567"
        );

        store.setCompanyAddress(
                "Puebla, México"
        );

        store.setCompanyWebsite(
                "https://settings-defaults.test"
        );

        store.setContactName(
                "Settings Owner"
        );

        store.setCurrency("MXN");

        store.setProposalFooter(
                "50% anticipo"
        );

        store = storeRepository.saveAndFlush(store);

        StoreSettings settings =
                storeSettingsService.createDefaults(store);

        assertThat(settings.getId())
                .isNotNull();

        assertThat(settings.getStore().getId())
                .isEqualTo(store.getId());

        assertThat(settings.getLogoUrl())
                .isEqualTo(
                        "https://cdn.test/default-logo.png"
                );

        assertThat(settings.getFaviconUrl())
                .isEqualTo(
                        "https://cdn.test/default-favicon.png"
                );

        assertThat(settings.getPrimaryColor())
                .isEqualTo("#123456");

        assertThat(settings.getSecondaryColor())
                .isEqualTo("#654321");

        assertThat(settings.getAccentColor())
                .isEqualTo("#ABCDEF");

        assertThat(settings.getFontFamily())
                .isEqualTo("Montserrat");

        assertThat(settings.getHeroImageUrl())
                .isEqualTo(
                        "https://cdn.test/default-hero.jpg"
                );

        assertThat(settings.getSlogan())
                .isEqualTo(
                        "Construimos el futuro"
                );

        assertThat(settings.getCompanyEmail())
                .isEqualTo(
                        "ventas@settings-defaults.test"
                );

        assertThat(settings.getCompanyPhone())
                .isEqualTo(
                        "+52 222 123 4567"
                );

        assertThat(settings.getCompanyAddress())
                .isEqualTo(
                        "Puebla, México"
                );

        assertThat(settings.getCompanyWebsite())
                .isEqualTo(
                        "https://settings-defaults.test"
                );

        assertThat(settings.getContactName())
                .isEqualTo(
                        "Settings Owner"
                );

        assertThat(settings.getCurrency())
                .isEqualTo("MXN");

        assertThat(settings.getProposalFooter())
                .isEqualTo(
                        "50% anticipo"
                );

        assertThat(settings.getCustomCss())
                .isEmpty();

        assertThat(settings.getCustomJs())
                .isEmpty();
    }

    /*
     * =========================================================
     * IDEMPOTENCY
     * =========================================================
     */

    @Test
    void createDefaultsShouldBeIdempotent() {

        Store store =
                createStore(
                        "Settings Idempotent",
                        "settings-idempotent"
                );

        Store persistedStore =
                storeRepository.saveAndFlush(store);

        StoreSettings first =
                storeSettingsService.createDefaults(
                        persistedStore
                );

        StoreSettings second =
                storeSettingsService.createDefaults(
                        persistedStore
                );

        assertThat(first.getId())
                .isNotNull();

        assertThat(second.getId())
                .isEqualTo(first.getId());

        assertThat(
                storeSettingsRepository.findByStoreId(
                        persistedStore.getId()
                )
        ).isPresent();

        long settingsForStore =
                storeSettingsRepository
                        .findAll()
                        .stream()
                        .filter(settings ->
                                settings.getStore() != null
                                && persistedStore
                                        .getId()
                                        .equals(
                                                settings
                                                        .getStore()
                                                        .getId()
                                        )
                        )
                        .count();

        assertThat(settingsForStore)
                .isEqualTo(1);
    }
    /*
     * =========================================================
     * PERSISTENCE
     * =========================================================
     */

    @Test
    void shouldPersistStoreCustomization() {

        Store store =
                createStore(
                        "Settings Persistence",
                        "settings-persistence"
                );

        store =
                storeRepository.saveAndFlush(store);

        StoreSettings settings =
                storeSettingsService.createDefaults(store);

        settings.setLogoUrl(
                "https://cdn.test/custom-logo.png"
        );

        settings.setFaviconUrl(
                "https://cdn.test/custom-favicon.png"
        );

        settings.setPrimaryColor("#101010");
        settings.setSecondaryColor("#202020");
        settings.setAccentColor("#30FF80");

        settings.setFontFamily("Poppins");

        settings.setHeroImageUrl(
                "https://cdn.test/custom-hero.jpg"
        );

        settings.setSlogan(
                "Tu negocio. Tu plataforma."
        );

        settings.setCompanyEmail(
                "ventas@custom.test"
        );

        settings.setCompanyPhone(
                "+52 222 987 6543"
        );

        settings.setCompanyAddress(
                "Puebla"
        );

        settings.setCompanyWebsite(
                "https://custom.test"
        );

        settings.setContactName(
                "Custom Owner"
        );

        settings.setCurrency("USD");

        settings.setProposalFooter(
                "Vigencia: 15 días"
        );

        settings.setGoogleAnalyticsId(
                "G-INTEGRATION"
        );

        settings.setMetaPixelId(
                "META-INTEGRATION"
        );

        settings.setTiktokPixelId(
                "TIKTOK-INTEGRATION"
        );

        settings.setHotjarId(
                "HOTJAR-INTEGRATION"
        );

        storeSettingsService.save(settings);

        /*
         * Flush + clear no son necesarios para demostrar que
         * JpaRepository.save() fue invocado; sí son deseables
         * conceptualmente para una prueba de persistencia.
         *
         * Como no inyectamos EntityManager todavía, usamos
         * saveAndFlush() sobre el repositorio para forzar SQL.
         */
        storeSettingsRepository.flush();

        StoreSettings persisted =
                storeSettingsRepository
                        .findByStoreId(store.getId())
                        .orElseThrow();

        assertThat(persisted.getLogoUrl())
                .isEqualTo(
                        "https://cdn.test/custom-logo.png"
                );

        assertThat(persisted.getFaviconUrl())
                .isEqualTo(
                        "https://cdn.test/custom-favicon.png"
                );

        assertThat(persisted.getPrimaryColor())
                .isEqualTo("#101010");

        assertThat(persisted.getSecondaryColor())
                .isEqualTo("#202020");

        assertThat(persisted.getAccentColor())
                .isEqualTo("#30FF80");

        assertThat(persisted.getFontFamily())
                .isEqualTo("Poppins");

        assertThat(persisted.getHeroImageUrl())
                .isEqualTo(
                        "https://cdn.test/custom-hero.jpg"
                );

        assertThat(persisted.getSlogan())
                .isEqualTo(
                        "Tu negocio. Tu plataforma."
                );

        assertThat(persisted.getCompanyEmail())
                .isEqualTo(
                        "ventas@custom.test"
                );

        assertThat(persisted.getCompanyPhone())
                .isEqualTo(
                        "+52 222 987 6543"
                );

        assertThat(persisted.getCompanyAddress())
                .isEqualTo("Puebla");

        assertThat(persisted.getCompanyWebsite())
                .isEqualTo(
                        "https://custom.test"
                );

        assertThat(persisted.getContactName())
                .isEqualTo(
                        "Custom Owner"
                );

        assertThat(persisted.getCurrency())
                .isEqualTo("USD");

        assertThat(persisted.getProposalFooter())
                .isEqualTo(
                        "Vigencia: 15 días"
                );

        assertThat(persisted.getGoogleAnalyticsId())
                .isEqualTo("G-INTEGRATION");

        assertThat(persisted.getMetaPixelId())
                .isEqualTo("META-INTEGRATION");

        assertThat(persisted.getTiktokPixelId())
                .isEqualTo("TIKTOK-INTEGRATION");

        assertThat(persisted.getHotjarId())
                .isEqualTo("HOTJAR-INTEGRATION");
    }

    /*
     * =========================================================
     * TENANT PERSISTENCE ISOLATION
     * =========================================================
     */

    @Test
    void modifyingOneStoreSettingsShouldNotModifyAnotherStore() {

        Store stride =
                createStore(
                        "Stride",
                        "settings-stride"
                );

        Store barleyPunch =
                createStore(
                        "Barley Punch",
                        "settings-barley"
                );

        stride =
                storeRepository.saveAndFlush(stride);

        barleyPunch =
                storeRepository.saveAndFlush(barleyPunch);

        StoreSettings strideSettings =
                storeSettingsService
                        .createDefaults(stride);

        StoreSettings barleySettings =
                storeSettingsService
                        .createDefaults(barleyPunch);

        strideSettings.setPrimaryColor(
                "#00FF88"
        );

        strideSettings.setSlogan(
                "Stride personalizado"
        );

        strideSettings.setCompanyEmail(
                "ventas@stride.test"
        );

        storeSettingsService.save(
                strideSettings
        );

        storeSettingsRepository.flush();

        StoreSettings persistedStride =
                storeSettingsRepository
                        .findByStoreId(
                                stride.getId()
                        )
                        .orElseThrow();

        StoreSettings persistedBarley =
                storeSettingsRepository
                        .findByStoreId(
                                barleyPunch.getId()
                        )
                        .orElseThrow();

        assertThat(persistedStride.getId())
                .isNotEqualTo(
                        persistedBarley.getId()
                );

        assertThat(
                persistedStride
                        .getStore()
                        .getId()
        ).isEqualTo(
                stride.getId()
        );

        assertThat(
                persistedBarley
                        .getStore()
                        .getId()
        ).isEqualTo(
                barleyPunch.getId()
        );

        assertThat(
                persistedStride
                        .getPrimaryColor()
        ).isEqualTo(
                "#00FF88"
        );

        assertThat(
                persistedStride
                        .getSlogan()
        ).isEqualTo(
                "Stride personalizado"
        );

        assertThat(
                persistedStride
                        .getCompanyEmail()
        ).isEqualTo(
                "ventas@stride.test"
        );

        /*
         * La personalización de Stride no debe propagarse
         * accidentalmente a Barley Punch.
         */
        assertThat(
                persistedBarley
                        .getPrimaryColor()
        ).isNotEqualTo(
                "#00FF88"
        );

        assertThat(
                persistedBarley
                        .getSlogan()
        ).isNotEqualTo(
                "Stride personalizado"
        );

        assertThat(
                persistedBarley
                        .getCompanyEmail()
        ).isNotEqualTo(
                "ventas@stride.test"
        );
    }

    /*
     * =========================================================
     * GET OR CREATE
     * =========================================================
     */

    @Test
    void getOrCreateShouldReturnExistingSettingsWithoutOverwritingThem() {

        Store store =
                createStore(
                        "Settings Existing",
                        "settings-existing"
                );

        store.setPrimaryColor("#111111");

        store =
                storeRepository.saveAndFlush(store);

        StoreSettings original =
                storeSettingsService
                        .createDefaults(store);

        original.setPrimaryColor("#ABC123");
        original.setSlogan(
                "Personalización existente"
        );

        storeSettingsService.save(original);
        storeSettingsRepository.flush();

        /*
         * Cambiamos posteriormente el Store.
         *
         * getOrCreate() NO debe volver a copiar estos defaults
         * encima de una personalización ya realizada.
         */
        store.setPrimaryColor("#FFFFFF");
        storeRepository.saveAndFlush(store);

        StoreSettings resolved =
                storeSettingsService
                        .getOrCreate(store);

        assertThat(resolved.getId())
                .isEqualTo(original.getId());

        assertThat(resolved.getPrimaryColor())
                .isEqualTo("#ABC123");

        assertThat(resolved.getSlogan())
                .isEqualTo(
                        "Personalización existente"
                );
    }

    /*
     * =========================================================
     * FIXTURE
     * =========================================================
     */

    private Store createStore(
            String name,
            String domainPrefix
    ) {

        long suffix =
                System.nanoTime();

        Store store =
                new Store();

        store.setNombre(name);

        store.setDominio(
                domainPrefix
                + "-"
                + suffix
                + ".web-empresarial.test"
        );

        /*
         * Defaults mínimos coherentes con provisioning.
         */
        store.setPrimaryColor("#111827");
        store.setSecondaryColor("#6B7280");
        store.setAccentColor("#2563EB");
        store.setFontFamily("Inter");
        store.setCurrency("MXN");

        return store;
    }
}
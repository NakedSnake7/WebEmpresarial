package com.webempresarial.store.controller;

import com.webempresarial.store.dto.CloudinaryUploadResult;
import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreSettingsControllerTest {

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private StoreSettingsService storeSettingsService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private FeatureAccessService features;

    private StoreSettingsController controller;

    private Store store;
    private StoreSettings settings;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        controller = new StoreSettingsController(
                storeContextService,
                storeSettingsService,
                cloudinaryService,
                features
        );

        store = new Store();
        store.setId(100L);
        store.setNombre("ACME");
        store.setDominio("acme.web-empresarial.com");

        settings = new StoreSettings();
        settings.setStore(store);
        settings.setCompanyEmail("old@acme.test");
        settings.setPrimaryColor("#111827");
        settings.setCustomCss("body { color: red; }");
        settings.setCustomJs("console.log('old');");

        request = new MockHttpServletRequest();
    }

    /**
     * Configura únicamente los mocks necesarios para aquellos casos
     * que realmente necesitan resolver el tenant actual.
     *
     * No se ejecuta globalmente para evitar UnnecessaryStubbingException
     * en pruebas que retornan antes de resolver el Store.
     */
    private void mockCurrentTenant() {
        when(storeContextService.getCurrentStore(
                any(HttpServletRequest.class)
        )).thenReturn(store);

        when(storeSettingsService.getOrCreate(store))
                .thenReturn(settings);
    }

    @Test
    void shouldLoadSettingsForCurrentTenant() {
        mockCurrentTenant();

        Model model = new ExtendedModelMap();

        String view = controller.settings(model, request);

        assertThat(view)
                .isEqualTo("admin/store/settings");

        assertThat(model.getAttribute("store"))
                .isSameAs(store);

        assertThat(model.getAttribute("settings"))
                .isSameAs(settings);

        assertThat(model.getAttribute("features"))
                .isSameAs(features);

        verify(storeContextService)
                .getCurrentStore(request);

        verify(storeSettingsService)
                .getOrCreate(store);
    }

    @Test
    void shouldSaveBasicSettingsForCurrentTenant() {
        mockCurrentTenant();

        StoreSettings form = new StoreSettings();

        form.setCompanyEmail("ventas@acme.test");
        form.setCompanyPhone("+52 222 123 4567");
        form.setCompanyAddress("Puebla");
        form.setCompanyWebsite("https://acme.test");
        form.setContactName("ACME Owner");
        form.setCurrency("MXN");
        form.setProposalFooter("50% anticipo");

        form.setFaviconUrl("https://cdn.test/favicon.png");
        form.setPrimaryColor("#123456");
        form.setSecondaryColor("#654321");
        form.setAccentColor("#ABCDEF");
        form.setFontFamily("Montserrat");
        form.setHeroImageUrl("https://cdn.test/hero.jpg");
        form.setSlogan("Construimos el futuro");
        form.setHeroEyebrow("NUEVA COLECCIÓN");
        form.setHeroTitle("HERO-TITLE-ACME");
        form.setHeroSubtitle("HERO-SUBTITLE-ACME");
        form.setHeroButtonText("Ver productos");
        form.setHeroButtonUrl("/#productos");

        form.setAboutTitle("ABOUT-TITLE-ACME");
        form.setAboutText("ABOUT-TEXT-ACME");

        form.setCtaTitle("CTA-TITLE-ACME");
        form.setCtaText("CTA-TEXT-ACME");

        form.setWhatsappMessage("WHATSAPP-MESSAGE-ACME");

        form.setFacebookUrl("https://facebook.com/acme");
        form.setInstagramUrl("https://instagram.com/acme");
        form.setTiktokUrl("https://tiktok.com/@acme");

        form.setFooterText("FOOTER-TEXT-ACME");

        form.setGoogleAnalyticsId("G-123456");
        form.setMetaPixelId("123456789");
        form.setTiktokPixelId("TIKTOK-123");
        form.setHotjarId("987654");

        when(features.canUse(store, "WHITE_LABEL"))
                .thenReturn(false);

        String result =
                controller.saveSettings(form, request);

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?success"
                );

        ArgumentCaptor<StoreSettings> captor =
                ArgumentCaptor.forClass(StoreSettings.class);

        verify(storeSettingsService)
                .save(captor.capture());

        StoreSettings saved = captor.getValue();

        /*
         * El controlador debe modificar los settings pertenecientes
         * al tenant resuelto por StoreContextService, no persistir
         * directamente el objeto recibido desde el formulario.
         */
        assertThat(saved)
                .isSameAs(settings);

        assertThat(saved.getCompanyEmail())
                .isEqualTo("ventas@acme.test");

        assertThat(saved.getCompanyPhone())
                .isEqualTo("+52 222 123 4567");

        assertThat(saved.getCompanyAddress())
                .isEqualTo("Puebla");

        assertThat(saved.getCompanyWebsite())
                .isEqualTo("https://acme.test");

        assertThat(saved.getContactName())
                .isEqualTo("ACME Owner");

        assertThat(saved.getCurrency())
                .isEqualTo("MXN");

        assertThat(saved.getProposalFooter())
                .isEqualTo("50% anticipo");

        assertThat(saved.getFaviconUrl())
                .isEqualTo("https://cdn.test/favicon.png");

        assertThat(saved.getPrimaryColor())
                .isEqualTo("#123456");

        assertThat(saved.getSecondaryColor())
                .isEqualTo("#654321");

        assertThat(saved.getAccentColor())
                .isEqualTo("#ABCDEF");

        assertThat(saved.getFontFamily())
                .isEqualTo("Montserrat");

        assertThat(saved.getHeroImageUrl())
                .isEqualTo("https://cdn.test/hero.jpg");

        assertThat(saved.getSlogan())
                .isEqualTo("Construimos el futuro");
        assertThat(saved.getHeroEyebrow())
        .isEqualTo("NUEVA COLECCIÓN");

assertThat(saved.getHeroTitle())
        .isEqualTo("HERO-TITLE-ACME");

assertThat(saved.getHeroSubtitle())
        .isEqualTo("HERO-SUBTITLE-ACME");

assertThat(saved.getHeroButtonText())
        .isEqualTo("Ver productos");

assertThat(saved.getHeroButtonUrl())
        .isEqualTo("/#productos");

assertThat(saved.getAboutTitle())
        .isEqualTo("ABOUT-TITLE-ACME");

assertThat(saved.getAboutText())
        .isEqualTo("ABOUT-TEXT-ACME");

assertThat(saved.getCtaTitle())
        .isEqualTo("CTA-TITLE-ACME");

assertThat(saved.getCtaText())
        .isEqualTo("CTA-TEXT-ACME");

assertThat(saved.getWhatsappMessage())
        .isEqualTo("WHATSAPP-MESSAGE-ACME");

assertThat(saved.getFacebookUrl())
        .isEqualTo("https://facebook.com/acme");

assertThat(saved.getInstagramUrl())
        .isEqualTo("https://instagram.com/acme");

assertThat(saved.getTiktokUrl())
        .isEqualTo("https://tiktok.com/@acme");

assertThat(saved.getFooterText())
        .isEqualTo("FOOTER-TEXT-ACME");

        assertThat(saved.getGoogleAnalyticsId())
                .isEqualTo("G-123456");

        assertThat(saved.getMetaPixelId())
                .isEqualTo("123456789");

        assertThat(saved.getTiktokPixelId())
                .isEqualTo("TIKTOK-123");

        assertThat(saved.getHotjarId())
                .isEqualTo("987654");

        verify(storeContextService)
                .getCurrentStore(request);

        verify(storeSettingsService)
                .getOrCreate(store);
    }

    @Test
    void shouldNotModifyWhiteLabelFieldsWhenFeatureIsUnavailable() {
        mockCurrentTenant();

        StoreSettings form = new StoreSettings();

        /*
         * Simulamos un POST manipulado manualmente.
         * Estos campos no aparecerían en la UI para un plan
         * sin WHITE_LABEL, pero un cliente podría intentar
         * enviarlos directamente.
         */
        form.setCustomCss("body { display:none; }");
        form.setCustomJs("alert('hacked');");

        when(features.canUse(store, "WHITE_LABEL"))
                .thenReturn(false);

        String result =
                controller.saveSettings(form, request);

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?success"
                );

        /*
         * Deben conservarse los valores anteriores.
         */
        assertThat(settings.getCustomCss())
                .isEqualTo("body { color: red; }");

        assertThat(settings.getCustomJs())
                .isEqualTo("console.log('old');");

        verify(features)
                .canUse(store, "WHITE_LABEL");

        verify(storeSettingsService)
                .save(settings);
    }

    @Test
    void shouldSaveWhiteLabelFieldsWhenFeatureIsAvailable() {
        mockCurrentTenant();

        StoreSettings form = new StoreSettings();

        form.setCustomCss(
                "body { background: black; }"
        );

        form.setCustomJs(
                "console.log('premium');"
        );

        when(features.canUse(store, "WHITE_LABEL"))
                .thenReturn(true);

        String result =
                controller.saveSettings(form, request);

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?success"
                );

        assertThat(settings.getCustomCss())
                .isEqualTo(
                        "body { background: black; }"
                );

        assertThat(settings.getCustomJs())
                .isEqualTo(
                        "console.log('premium');"
                );

        verify(features)
                .canUse(store, "WHITE_LABEL");

        verify(storeSettingsService)
                .save(settings);
    }

    @Test
    void shouldUploadLogoForCurrentTenant() throws Exception {
        mockCurrentTenant();

        MockMultipartFile logo =
                new MockMultipartFile(
                        "logoFile",
                        "logo.png",
                        "image/png",
                        "fake-image".getBytes()
                );

        CloudinaryUploadResult uploadResult =
                mock(CloudinaryUploadResult.class);

        when(uploadResult.getSecureUrl())
                .thenReturn(
                        "https://cdn.test/acme-logo.png"
                );

        when(cloudinaryService.subirLogoTienda(
                logo,
                store.getId()
        )).thenReturn(uploadResult);

        String result =
                controller.uploadLogo(logo, request);

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?logoSuccess"
                );

        assertThat(settings.getLogoUrl())
                .isEqualTo(
                        "https://cdn.test/acme-logo.png"
                );

        verify(storeContextService)
                .getCurrentStore(request);

        verify(storeSettingsService)
                .getOrCreate(store);

        verify(cloudinaryService)
                .subirLogoTienda(
                        logo,
                        100L
                );

        verify(storeSettingsService)
                .save(settings);
    }

    @Test
    void shouldReplaceExistingLogo() throws Exception {
        mockCurrentTenant();

        settings.setLogoUrl(
                "https://res.cloudinary.com/demo/"
                        + "image/upload/v1/stores/100/old.png"
        );

        String oldLogoUrl =
                settings.getLogoUrl();

        MockMultipartFile logo =
                new MockMultipartFile(
                        "logoFile",
                        "new.png",
                        "image/png",
                        "new-image".getBytes()
                );

        when(cloudinaryService
                .extraerPublicIdDesdeUrl(oldLogoUrl))
                .thenReturn("stores/100/old");

        CloudinaryUploadResult uploadResult =
                mock(CloudinaryUploadResult.class);

        when(uploadResult.getSecureUrl())
                .thenReturn(
                        "https://cdn.test/new.png"
                );

        when(cloudinaryService.subirLogoTienda(
                logo,
                100L
        )).thenReturn(uploadResult);

        String result =
                controller.uploadLogo(logo, request);

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?logoSuccess"
                );

        verify(cloudinaryService)
                .extraerPublicIdDesdeUrl(oldLogoUrl);

        verify(cloudinaryService)
                .eliminarImagen("stores/100/old");

        verify(cloudinaryService)
                .subirLogoTienda(
                        logo,
                        100L
                );

        assertThat(settings.getLogoUrl())
                .isEqualTo(
                        "https://cdn.test/new.png"
                );

        verify(storeSettingsService)
                .save(settings);
    }

    @Test
    void shouldKeepExistingLogoWhenReplacementUploadFails() throws Exception {
        mockCurrentTenant();

        String oldLogoUrl =
                "https://res.cloudinary.com/demo/"
                        + "image/upload/v1/stores/100/old.png";

        settings.setLogoUrl(oldLogoUrl);

        when(cloudinaryService
                .extraerPublicIdDesdeUrl(oldLogoUrl))
                .thenReturn("stores/100/old");

        MockMultipartFile logo =
                new MockMultipartFile(
                        "logoFile",
                        "new.png",
                        "image/png",
                        "new-image".getBytes()
                );

        when(cloudinaryService
                .subirLogoTienda(
                        logo,
                        100L
                ))
                .thenThrow(
                        new java.io.IOException(
                                "Cloudinary unavailable"
                        )
                );

        String result =
                controller.uploadLogo(
                        logo,
                        request
                );

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?logoError"
                );

        assertThat(settings.getLogoUrl())
                .isEqualTo(oldLogoUrl);

        verify(cloudinaryService, never())
                .eliminarImagen(anyString());

        verify(storeSettingsService, never())
                .save(settings);
    }

    @Test
    void shouldRejectEmptyLogo() {
        MockMultipartFile emptyLogo =
                new MockMultipartFile(
                        "logoFile",
                        "",
                        "image/png",
                        new byte[0]
                );

        String result =
                controller.uploadLogo(
                        emptyLogo,
                        request
                );

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?logoError"
                );

        /*
         * El controlador debe rechazar el archivo antes incluso
         * de resolver el tenant o consultar sus settings.
         */
        verifyNoInteractions(storeContextService);
        verifyNoInteractions(storeSettingsService);
        verifyNoInteractions(cloudinaryService);
        verifyNoInteractions(features);
    }

    @Test
    void shouldDeleteLogoForCurrentTenant() throws Exception {
        mockCurrentTenant();

        settings.setLogoUrl(
                "https://res.cloudinary.com/demo/"
                        + "image/upload/v1/stores/100/logo.png"
        );

        String logoUrl =
                settings.getLogoUrl();

        when(cloudinaryService
                .extraerPublicIdDesdeUrl(logoUrl))
                .thenReturn("stores/100/logo");

        String result =
                controller.deleteLogo(request);

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?logoDeleted"
                );

        verify(cloudinaryService)
                .extraerPublicIdDesdeUrl(logoUrl);

        verify(cloudinaryService)
                .eliminarImagen("stores/100/logo");

        assertThat(settings.getLogoUrl())
                .isNull();

        verify(storeSettingsService)
                .save(settings);
    }

    @Test
    void shouldDeleteSettingsLogoEvenWhenCloudinaryPublicIdCannotBeResolved()
            throws Exception {

        mockCurrentTenant();

        settings.setLogoUrl(
                "https://legacy.test/logo.png"
        );

        String logoUrl =
                settings.getLogoUrl();

        when(cloudinaryService
                .extraerPublicIdDesdeUrl(logoUrl))
                .thenReturn(null);

        String result =
                controller.deleteLogo(request);

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/store/settings?logoDeleted"
                );

        verify(cloudinaryService)
                .extraerPublicIdDesdeUrl(logoUrl);

        verify(cloudinaryService, never())
                .eliminarImagen(anyString());

        assertThat(settings.getLogoUrl())
                .isNull();

        verify(storeSettingsService)
                .save(settings);
    }
}
package com.webempresarial.store.controller;


import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.ThemeType;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;
import com.webempresarial.store.theme.ThemeModelAdvice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import com.webempresarial.store.dto.producto.publico.ProductoCardDTO;
import com.webempresarial.store.entity.ResenaEntity;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(
        controllers = HomeController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@Import({
        StoreViewAdvice.class,
        ThemeModelAdvice.class
})
class StorefrontReflectionMockMvcTest {

    private static final String TENANT_A_HOST =
            "stride-reflection.web-empresarial.com";

    private static final String TENANT_B_HOST =
            "barley-reflection.web-empresarial.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private ResenaRepository resenaRepository;

    @MockitoBean
    private StoreResolver storeResolver;

    @MockitoBean
    private StoreThemeResolver storeThemeResolver;

    @MockitoBean
    private DashboardRegistry dashboardRegistry;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private StoreSettingsService storeSettingsService;

    @MockitoBean
    private FeatureAccessService featureAccessService;

    /*
     * FeatureViewAdvice puede ser descubierto por @WebMvcTest
     * aunque no forme parte explícita del @Import.
     */
    @MockitoBean
    private SidebarRegistry sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService inventoryAlertService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    private Store storeA;
    private Store storeB;

    private StoreSettings settingsA;
    private StoreSettings settingsB;

    private ProductoCardDTO productA;
    private ProductoCardDTO productB;

    private ResenaEntity reviewA;
    private ResenaEntity reviewB;

    @BeforeEach
    void setUp() throws Exception {

        storeA = store(
                101L,
                "STRIDE REFLECTION A"
        );

        storeB = store(
                202L,

                "BARLEY REFLECTION B"
        );

        settingsA = settings(
                storeA,
                "https://cdn.example.test/STRIDE-UNIQUE-LOGO-A.png",
                "https://cdn.example.test/STRIDE-UNIQUE-HERO-A.jpg",
                "STRIDE-REFLECTION-SLOGAN-A",
                "#123456",
                "#654321",
                "#ABCDEF",
                "Inter",
                "TENANT-A"
        );

        settingsB = settings(
                storeB,
                "https://cdn.example.test/BARLEY-UNIQUE-LOGO-B.png",
                "https://cdn.example.test/BARLEY-UNIQUE-HERO-B.jpg",
                "BARLEY-REFLECTION-SLOGAN-B",
                "#112233",
                "#445566",
                "#778899",
                "Roboto",
                "TENANT-B"
        );

        productA = product(
                1001L,
                "PRODUCT-A-UNIQUE",
                "https://cdn.example.test/PRODUCT-A-UNIQUE.jpg",
                "CATEGORY-A-UNIQUE",
                "BRAND-A-UNIQUE",
                new BigDecimal("1299.90"),
                8
        );

        productB = product(
                2002L,
                "PRODUCT-B-UNIQUE",
                "https://cdn.example.test/PRODUCT-B-UNIQUE.jpg",
                "CATEGORY-B-UNIQUE",
                "BRAND-B-UNIQUE",
                new BigDecimal("2499.50"),
                0
        );

        reviewA = review(
                "CUSTOMER-A-UNIQUE",
                "REVIEW-A-UNIQUE",
                5
        );

        reviewB = review(
                "CUSTOMER-B-UNIQUE",
                "REVIEW-B-UNIQUE",
                4
        );

        /*
         * HomeController usa StoreResolver.
         * StoreViewAdvice usa StoreContextService.
         *
         * Ambos deben resolver exactamente el mismo tenant
         * a partir del request actual.
         */
        when(storeResolver.getCurrentStore(any()))
                .thenAnswer(invocation ->
                        resolveStore(
                                invocation.getArgument(0)
                        )
                );

        when(storeContextService.getCurrentStore(any()))
                .thenAnswer(invocation ->
                        resolveStore(
                                invocation.getArgument(0)
                        )
                );

        /*
         * ThemeModelAdvice y HomeController utilizan
         * StoreThemeResolver.
         */
        when(storeThemeResolver.resolveTheme(storeA))
                .thenReturn("basic");

        when(storeThemeResolver.resolveTheme(storeB))
                .thenReturn("basic");

        when(storeThemeResolver.getTheme(any()))
                .thenReturn("basic");

        /*
         * StoreSettings también queda aislado por Store.
         */
        when(storeSettingsService.getOrCreate(storeA))
                .thenReturn(settingsA);

        when(storeSettingsService.getOrCreate(storeB))
                .thenReturn(settingsB);

        /*
         * No necesitamos WHITE_LABEL para demostrar
         * la reflexión del branding básico.
         */
        when(featureAccessService.canUse(
                any(Store.class),
                any(String.class)
        )).thenReturn(false);

        /*
         * HomeController sólo necesita colecciones vacías
         * para poder renderizar el storefront.
         */
        /*
         * Contenido storefront aislado por tenant.
         *
         * Las listas NO deben quedar vacías: queremos que Thymeleaf
         * ejecute realmente categorías, productos, stock y reseñas.
         */
        when(productoService.obtenerCategorias(storeA))
                .thenReturn(List.of("CATEGORY-A-UNIQUE"));

        when(productoService.obtenerCategorias(storeB))
                .thenReturn(List.of("CATEGORY-B-UNIQUE"));

        when(productoService.obtenerProductosIndexOptimizado(storeA))
                .thenReturn(List.of(productA));

        when(productoService.obtenerProductosIndexOptimizado(storeB))
                .thenReturn(List.of(productB));

        when(resenaRepository.findByStoreOrderByEstrellasDesc(
                eq(storeA),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(List.of(reviewA))
        );

        when(resenaRepository.findByStoreOrderByEstrellasDesc(
                eq(storeB),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(List.of(reviewB))
        );

when(storeThemeResolver.view(
        any(HttpServletRequest.class),
        eq("index")
)).thenReturn("themes/basic/index");

when(adminTenantAccessInterceptor.preHandle(
        any(),
        any(),
        any()
)).thenReturn(true);

when(subscriptionInterceptor.preHandle(
        any(),
        any(),
        any()
)).thenReturn(true);
    }

    @Test
    void shouldRenderTenantABrandingWithoutTenantBLeakage()
            throws Exception {

        mockMvc.perform(
                get("/")
                        .header("Host", TENANT_A_HOST)
                        .with(request -> {
                            request.setServerName(TENANT_A_HOST);
                            return request;
                        })
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "themes/basic/index"
        ))

        /*
         * =========================================================
         * BRANDING TENANT A
         * =========================================================
         */
        .andExpect(content().string(
                containsString("STRIDE-UNIQUE-LOGO-A.png")
        ))
        .andExpect(content().string(
                containsString("STRIDE-UNIQUE-HERO-A.jpg")
        ))
        .andExpect(content().string(
                containsString("#123456")
        ))
        .andExpect(content().string(
                containsString("#654321")
        ))
        .andExpect(content().string(
                containsString("#ABCDEF")
        ))

        /*
         * =========================================================
         * CONTENIDO ADMINISTRABLE TENANT A
         * =========================================================
         */
        .andExpect(content().string(
                containsString("HERO-EYEBROW-TENANT-A")
        ))
        .andExpect(content().string(
                containsString("HERO-TITLE-TENANT-A")
        ))
        .andExpect(content().string(
                containsString("HERO-SUBTITLE-TENANT-A")
        ))
        .andExpect(content().string(
                containsString("HERO-BUTTON-TENANT-A")
        ))
        .andExpect(content().string(
                containsString("/storefront-tenant-a")
        ))

        .andExpect(content().string(
                containsString("ABOUT-TITLE-TENANT-A")
        ))
        .andExpect(content().string(
                containsString("ABOUT-TEXT-TENANT-A")
        ))

        .andExpect(content().string(
                containsString("CTA-TITLE-TENANT-A")
        ))
        .andExpect(content().string(
                containsString("CTA-TEXT-TENANT-A")
        ))

        .andExpect(content().string(
                containsString("FOOTER-TEXT-TENANT-A")
        ))
        .andExpect(content().string(
                containsString(
                        "https://facebook.example/tenant-a"
                )
        ))
        .andExpect(content().string(
                containsString(
                        "https://instagram.example/tenant-a"
                )
        ))
        .andExpect(content().string(
                containsString(
                        "https://tiktok.example/@tenant-a"
                )
        ))
        .andExpect(content().string(
                containsString("522221111111")
        ))
        .andExpect(content().string(
                containsString("WHATSAPP-MESSAGE-TENANT-A")
        ))
        .andExpect(content().string(
                containsString("+52 222 111 1111")
        ))

        /*
         * =========================================================
         * CONTENIDO COMERCIAL TENANT A
         * =========================================================
         */
        .andExpect(content().string(
                containsString("CATEGORY-A-UNIQUE")
        ))
        .andExpect(content().string(
                containsString("PRODUCT-A-UNIQUE")
        ))
        .andExpect(content().string(
                containsString("PRODUCT-A-UNIQUE.jpg")
        ))
        .andExpect(content().string(
                containsString("CUSTOMER-A-UNIQUE")
        ))
        .andExpect(content().string(
                containsString("REVIEW-A-UNIQUE")
        ))
        .andExpect(content().string(
                containsString("Disponible")
        ))

        /*
         * =========================================================
         * TENANT B NO DEBE FILTRARSE EN TENANT A
         * =========================================================
         */
        .andExpect(content().string(
                not(containsString("BARLEY-UNIQUE-LOGO-B.png"))
        ))
        .andExpect(content().string(
                not(containsString("BARLEY-UNIQUE-HERO-B.jpg"))
        ))
        .andExpect(content().string(
                not(containsString("#112233"))
        ))

        .andExpect(content().string(
                not(containsString("HERO-EYEBROW-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("HERO-TITLE-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("HERO-SUBTITLE-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("HERO-BUTTON-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("/storefront-tenant-b"))
        ))

        .andExpect(content().string(
                not(containsString("ABOUT-TITLE-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("ABOUT-TEXT-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("CTA-TITLE-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("CTA-TEXT-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("FOOTER-TEXT-TENANT-B"))
        ))

        .andExpect(content().string(
                not(containsString(
                        "https://facebook.example/tenant-b"
                ))
        ))
        .andExpect(content().string(
                not(containsString(
                        "https://instagram.example/tenant-b"
                ))
        ))
        .andExpect(content().string(
                not(containsString(
                        "https://tiktok.example/@tenant-b"
                ))
        ))

        .andExpect(content().string(
                not(containsString("CATEGORY-B-UNIQUE"))
        ))
        .andExpect(content().string(
                not(containsString("PRODUCT-B-UNIQUE"))
        ))
        .andExpect(content().string(
                not(containsString("PRODUCT-B-UNIQUE.jpg"))
        ))
        .andExpect(content().string(
                not(containsString("CUSTOMER-B-UNIQUE"))
        ))
        .andExpect(content().string(
                not(containsString("REVIEW-B-UNIQUE"))
        ))
        .andExpect(content().string(
                not(containsString("522222222222"))
        ))
        .andExpect(content().string(
                not(containsString("WHATSAPP-MESSAGE-TENANT-B"))
        ))
        .andExpect(content().string(
                not(containsString("+52 222 222 2222"))
        ));
    }

    @Test
    void shouldRenderTenantBBrandingWithoutTenantALeakage()
            throws Exception {

        mockMvc.perform(
                get("/")
                        .header("Host", TENANT_B_HOST)
                        .with(request -> {
                            request.setServerName(TENANT_B_HOST);
                            return request;
                        })
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "themes/basic/index"
        ))

        /*
         * =========================================================
         * BRANDING TENANT B
         * =========================================================
         */
        .andExpect(content().string(
                containsString("BARLEY-UNIQUE-LOGO-B.png")
        ))
        .andExpect(content().string(
                containsString("BARLEY-UNIQUE-HERO-B.jpg")
        ))
        .andExpect(content().string(
                containsString("#112233")
        ))
        .andExpect(content().string(
                containsString("#445566")
        ))
        .andExpect(content().string(
                containsString("#778899")
        ))

        /*
         * =========================================================
         * CONTENIDO ADMINISTRABLE TENANT B
         * =========================================================
         */
        .andExpect(content().string(
                containsString("HERO-EYEBROW-TENANT-B")
        ))
        .andExpect(content().string(
                containsString("HERO-TITLE-TENANT-B")
        ))
        .andExpect(content().string(
                containsString("HERO-SUBTITLE-TENANT-B")
        ))
        .andExpect(content().string(
                containsString("HERO-BUTTON-TENANT-B")
        ))
        .andExpect(content().string(
                containsString("/storefront-tenant-b")
        ))

        .andExpect(content().string(
                containsString("ABOUT-TITLE-TENANT-B")
        ))
        .andExpect(content().string(
                containsString("ABOUT-TEXT-TENANT-B")
        ))

        .andExpect(content().string(
                containsString("CTA-TITLE-TENANT-B")
        ))
        .andExpect(content().string(
                containsString("CTA-TEXT-TENANT-B")
        ))

        .andExpect(content().string(
                containsString("FOOTER-TEXT-TENANT-B")
        ))
        .andExpect(content().string(
                containsString(
                        "https://facebook.example/tenant-b"
                )
        ))
        .andExpect(content().string(
                containsString(
                        "https://instagram.example/tenant-b"
                )
        ))
        .andExpect(content().string(
                containsString(
                        "https://tiktok.example/@tenant-b"
                )
        ))

        .andExpect(content().string(
                containsString("522222222222")
        ))
        .andExpect(content().string(
                containsString("WHATSAPP-MESSAGE-TENANT-B")
        ))
        .andExpect(content().string(
                containsString("+52 222 222 2222")
        ))

        /*
         * =========================================================
         * CONTENIDO COMERCIAL TENANT B
         * =========================================================
         */
        .andExpect(content().string(
                containsString("CATEGORY-B-UNIQUE")
        ))
        .andExpect(content().string(
                containsString("PRODUCT-B-UNIQUE")
        ))
        .andExpect(content().string(
                containsString("PRODUCT-B-UNIQUE.jpg")
        ))
        .andExpect(content().string(
                containsString("CUSTOMER-B-UNIQUE")
        ))
        .andExpect(content().string(
                containsString("REVIEW-B-UNIQUE")
        ))
        .andExpect(content().string(
                containsString("Agotado")
        ))

        /*
         * =========================================================
         * TENANT A NO DEBE FILTRARSE EN TENANT B
         * =========================================================
         */
        .andExpect(content().string(
                not(containsString("STRIDE-UNIQUE-LOGO-A.png"))
        ))
        .andExpect(content().string(
                not(containsString("STRIDE-UNIQUE-HERO-A.jpg"))
        ))
        .andExpect(content().string(
                not(containsString("#123456"))
        ))

        .andExpect(content().string(
                not(containsString("HERO-EYEBROW-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("HERO-TITLE-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("HERO-SUBTITLE-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("HERO-BUTTON-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("/storefront-tenant-a"))
        ))

        .andExpect(content().string(
                not(containsString("ABOUT-TITLE-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("ABOUT-TEXT-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("CTA-TITLE-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("CTA-TEXT-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("FOOTER-TEXT-TENANT-A"))
        ))

        .andExpect(content().string(
                not(containsString(
                        "https://facebook.example/tenant-a"
                ))
        ))
        .andExpect(content().string(
                not(containsString(
                        "https://instagram.example/tenant-a"
                ))
        ))
        .andExpect(content().string(
                not(containsString(
                        "https://tiktok.example/@tenant-a"
                ))
        ))

        .andExpect(content().string(
                not(containsString("CATEGORY-A-UNIQUE"))
        ))
        .andExpect(content().string(
                not(containsString("PRODUCT-A-UNIQUE"))
        ))
        .andExpect(content().string(
                not(containsString("PRODUCT-A-UNIQUE.jpg"))
        ))
        .andExpect(content().string(
                not(containsString("CUSTOMER-A-UNIQUE"))
        ))
        .andExpect(content().string(
                not(containsString("REVIEW-A-UNIQUE"))
        ))
        .andExpect(content().string(
                not(containsString("522221111111"))
        ))
        .andExpect(content().string(
                not(containsString("WHATSAPP-MESSAGE-TENANT-A"))
        ))
        .andExpect(content().string(
                not(containsString("+52 222 111 1111"))
        ));
    }

    private Store resolveStore(
            HttpServletRequest request
    ) {

        String host = request.getServerName();

        if (TENANT_A_HOST.equalsIgnoreCase(host)) {
            return storeA;
        }

        if (TENANT_B_HOST.equalsIgnoreCase(host)) {
            return storeB;
        }

        throw new IllegalStateException(
                "Host inesperado en test: " + host
        );
    }

    private Store store(
            Long id,
            String nombre
    ) {

        Store store = new Store();

        store.setId(id);
        store.setNombre(nombre);
        store.setThemeType(ThemeType.BASIC);
        store.setPlan(StorePlan.BASIC);
        store.setStripeConnected(false);

        return store;
    }
    private ProductoCardDTO product(
            Long id,
            String productName,
            String imageUrl,
            String categoriaNombre,
            String marcaNombre,
            BigDecimal precio,
            int stock
    ) {

        ProductoCardDTO product =
                new ProductoCardDTO();

        product.setId(id);
        product.setProductName(productName);
        product.setImageUrl(imageUrl);

        product.setCategoriaNombre(categoriaNombre);
        product.setMarcaNombre(marcaNombre);

        product.setPrecio(precio);
        product.setPrecioMinimo(precio);

        product.setTieneVariantes(false);
        product.setTienePromocion(false);
        product.setStockSimple(stock);

        return product;
    }

    private ResenaEntity review(
            String nombre,
            String comentario,
            int estrellas
    ) {

        ResenaEntity review =
                new ResenaEntity();

        review.setNombre(nombre);
        review.setComentario(comentario);
        review.setEstrellas(estrellas);

        return review;
    }

    private StoreSettings settings(
            Store store,
            String logoUrl,
            String heroImageUrl,
            String slogan,
            String primaryColor,
            String secondaryColor,
            String accentColor,
            String fontFamily,
            String marker
    ) {

        StoreSettings settings =
                new StoreSettings();

        settings.setStore(store);

        /*
         * Branding básico.
         */
        settings.setLogoUrl(logoUrl);
        settings.setHeroImageUrl(heroImageUrl);
        settings.setSlogan(slogan);

        settings.setPrimaryColor(primaryColor);
        settings.setSecondaryColor(secondaryColor);
        settings.setAccentColor(accentColor);
        settings.setFontFamily(fontFamily);

        /*
         * Contenido administrable del storefront.
         *
         * marker permite generar valores inequívocos
         * para cada tenant y detectar cualquier fuga.
         */
        settings.setHeroEyebrow(
                "HERO-EYEBROW-" + marker
        );

        settings.setHeroTitle(
                "HERO-TITLE-" + marker
        );

        settings.setHeroSubtitle(
                "HERO-SUBTITLE-" + marker
        );

        settings.setHeroButtonText(
                "HERO-BUTTON-" + marker
        );

        settings.setHeroButtonUrl(
                "/storefront-" + marker.toLowerCase()
        );

        settings.setAboutTitle(
                "ABOUT-TITLE-" + marker
        );

        settings.setAboutText(
                "ABOUT-TEXT-" + marker
        );

        settings.setCtaTitle(
                "CTA-TITLE-" + marker
        );

        settings.setCtaText(
                "CTA-TEXT-" + marker
        );
        if ("TENANT-A".equals(marker)) {
            settings.setCompanyPhone(
                    "+52 222 111 1111"
            );
        } else if ("TENANT-B".equals(marker)) {
            settings.setCompanyPhone(
                    "+52 222 222 2222"
            );
        }
        settings.setWhatsappMessage(
                "WHATSAPP-MESSAGE-" + marker
        );

        settings.setFacebookUrl(
                "https://facebook.example/" + marker.toLowerCase()
        );

        settings.setInstagramUrl(
                "https://instagram.example/" + marker.toLowerCase()
        );

        settings.setTiktokUrl(
                "https://tiktok.example/@" + marker.toLowerCase()
        );

        settings.setFooterText(
                "FOOTER-TEXT-" + marker
        );

        return settings;
    }
}
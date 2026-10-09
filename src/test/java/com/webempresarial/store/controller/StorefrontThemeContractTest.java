package com.webempresarial.store.controller;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.dto.producto.publico.ProductoCardDTO;
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
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;
import com.webempresarial.store.theme.ThemeModelAdvice;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
class StorefrontThemeContractTest {

    private static final String HOST =
            "theme-contract.web-empresarial.com";

    private static final String PRODUCT_NAME =
            "CONTRACT-PRODUCT-9471";

    private static final String PRODUCT_IMAGE =
            "https://cdn.example.test/CONTRACT-PRODUCT-9471.png";

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

    @MockitoBean
    private SidebarRegistry sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService inventoryAlertService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    private Store store;
    private ProductoCardDTO product;

    @BeforeEach
    void setUp() throws Exception {

        store = new Store();
        store.setId(9471L);
        store.setNombre("THEME CONTRACT STORE");
        store.setPlan(StorePlan.PREMIUM);
        store.setThemeType(ThemeType.CUSTOM);

        product = new ProductoCardDTO(
                9471L,
                PRODUCT_NAME,
                new BigDecimal("321.45"),
                new BigDecimal("321.45"),
                false,
                false,
                0.0,
                PRODUCT_IMAGE,
                "CONTRACT-CATEGORY",
                "CONTRACT-BRAND",
                7
        );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(storeContextService.getCurrentStore(any()))
                .thenReturn(store);

        StoreSettings settings = mock(StoreSettings.class);

        when(storeSettingsService.getOrCreate(store))
                .thenReturn(settings);

        when(featureAccessService.canUse(
                any(Store.class),
                any(String.class)
        )).thenReturn(false);

        when(productoService.obtenerCategorias(store))
                .thenReturn(List.of("CONTRACT-CATEGORY"));

        when(productoService.obtenerProductosIndexOptimizado(store))
                .thenReturn(List.of(product));

        when(resenaRepository.findByStoreOrderByEstrellasDesc(
                eq(store),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

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

    static Stream<String> storefrontThemes() throws IOException {

        Path themesRoot =
                Path.of("src/main/resources/templates/themes");

        try (Stream<Path> paths = Files.list(themesRoot)) {

            return paths
                    .filter(Files::isDirectory)
                    .filter(path ->
                            Files.isRegularFile(
                                    path.resolve("index.html")
                            )
                    )
                    .map(path ->
                            path.getFileName().toString()
                    )
                    .filter(theme ->
                            !"WebEmpresarial".equals(theme)
                    )
                    .sorted()
                    .toList()
                    .stream();
        }
    }

    @ParameterizedTest(name = "{0} must render Commerce Engine product")
    @MethodSource("storefrontThemes")
    void shouldRenderCommerceEngineProduct(String theme)
            throws Exception {

        store.setTheme(theme);

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn(theme);

        when(storeThemeResolver.getTheme(any()))
                .thenReturn(theme);

        when(storeThemeResolver.view(
                any(HttpServletRequest.class),
                eq("index")
        )).thenReturn(
                "themes/" + theme + "/index"
        );

        mockMvc.perform(
                get("/")
                        .header("Host", HOST)
                        .with(request -> {
                            request.setServerName(HOST);
                            return request;
                        })
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "themes/" + theme + "/index"
        ))
        .andExpect(content().string(
                containsString(PRODUCT_NAME)
        ))
        .andExpect(content().string(
                containsString(PRODUCT_IMAGE)
        ));
    }
}

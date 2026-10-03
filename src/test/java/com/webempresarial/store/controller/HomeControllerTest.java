package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.ThemeType;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private StoreThemeResolver storeThemeResolver;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private DashboardRegistry dashboardRegistry;

    @Mock
    private HttpServletRequest request;

    @SuppressWarnings("rawtypes")
    @Mock
    private Page resenasPage;

    private HomeController controller;

    @BeforeEach
    void setUp() {

        controller =
                new HomeController(
                        productoService,
                        resenaRepository,
                        storeThemeResolver,
                        storeResolver,
                        dashboardRegistry
                );
    }

    @Test
    void shouldRenderBasicThemeWhenLegacyThemeIsNull() {

        Store store = new Store();

        store.setNombre("Basic Store");
        store.setTheme(null);
        store.setThemeType(ThemeType.BASIC);

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn("basic");

        when(storeThemeResolver.view(request, "index"))
                .thenReturn("themes/basic/index");

        when(productoService.obtenerCategorias(store))
                .thenReturn(Collections.emptyList());

        when(productoService.obtenerProductosIndexOptimizado(store))
                .thenReturn(Collections.emptyList());

        when(
                resenaRepository.findByStoreOrderByEstrellasDesc(
                        any(Store.class),
                        any()
                )
        ).thenReturn(resenasPage);

        when(resenasPage.getContent())
                .thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();

        String view =
                controller.home(model, request);

        assertThat(view)
                .isEqualTo("themes/basic/index");

        assertThat(model.getAttribute("store"))
                .isSameAs(store);

        assertThat(model.getAttribute("theme"))
                .isEqualTo("basic");

        assertThat(model.getAttribute("showCart"))
                .isEqualTo(true);

        assertThat(model.getAttribute("showCheckout"))
                .isEqualTo(true);

        assertThat(model.getAttribute("showAuth"))
                .isEqualTo(true);

        verify(storeThemeResolver, times(2))
        .resolveTheme(store);

        verify(storeThemeResolver)
                .view(request, "index");
    }

    @Test
    void shouldRenderProTheme() {

        Store store = new Store();

        store.setNombre("Pro Store");
        store.setThemeType(ThemeType.PRO);

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn("pro");

        when(storeThemeResolver.view(request, "index"))
                .thenReturn("themes/pro/index");

        mockCommerceData(store);

        Model model = new ConcurrentModel();

        String view =
                controller.home(model, request);

        assertThat(view)
                .isEqualTo("themes/pro/index");

        assertThat(model.getAttribute("theme"))
                .isEqualTo("pro");

        assertThat(model.getAttribute("showCart"))
                .isEqualTo(true);
    }

    @Test
    void shouldRenderPremiumTheme() {

        Store store = new Store();

        store.setNombre("Premium Store");
        store.setThemeType(ThemeType.PREMIUM);

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn("premium");

        when(storeThemeResolver.view(request, "index"))
                .thenReturn("themes/premium/index");

        mockCommerceData(store);

        Model model = new ConcurrentModel();

        String view =
                controller.home(model, request);

        assertThat(view)
                .isEqualTo("themes/premium/index");

        assertThat(model.getAttribute("theme"))
                .isEqualTo("premium");

        assertThat(model.getAttribute("showCart"))
                .isEqualTo(true);
    }

    @Test
    void shouldRenderWebEmpresarialWithoutLoadingCommerceData() {

        Store store = new Store();

        store.setNombre("WebEmpresarial");

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn("WebEmpresarial");

        when(storeThemeResolver.view(request, "index"))
                .thenReturn("themes/WebEmpresarial/index");

        Model model = new ConcurrentModel();

        String view =
                controller.home(model, request);

        assertThat(view)
                .isEqualTo("themes/WebEmpresarial/index");

        assertThat(model.getAttribute("theme"))
                .isEqualTo("WebEmpresarial");

        assertThat(model.getAttribute("showCart"))
                .isEqualTo(false);

        assertThat(model.getAttribute("showCheckout"))
                .isEqualTo(false);

        assertThat(model.getAttribute("showAuth"))
                .isEqualTo(false);

        assertThat(model.getAttribute("title"))
                .isEqualTo(
                        "WebEmpresarial™ | Ecommerce, páginas web y sistemas empresariales"
                );

        verify(productoService, never())
                .obtenerCategorias(any());

        verify(productoService, never())
                .obtenerProductosIndexOptimizado(any());

        verify(resenaRepository, never())
                .findByStoreOrderByEstrellasDesc(
                        any(Store.class),
                        any()
                );
    }

    @SuppressWarnings({
            "unchecked",
            "rawtypes"
    })
    private void mockCommerceData(Store store) {

        when(productoService.obtenerCategorias(store))
                .thenReturn(Collections.emptyList());

        when(productoService.obtenerProductosIndexOptimizado(store))
                .thenReturn(Collections.emptyList());

        when(
                resenaRepository.findByStoreOrderByEstrellasDesc(
                        any(Store.class),
                        any()
                )
        ).thenReturn(resenasPage);

        when(resenasPage.getContent())
                .thenReturn(Collections.emptyList());
    }
}
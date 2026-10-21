package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.ThemeType;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

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
    private FeatureAccessService featureAccessService;

    @Mock
    private DashboardRegistry dashboardRegistry;

    @Mock
    private OrderService orderService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

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
                        featureAccessService,
                        dashboardRegistry,
                        orderService
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
                featureAccessService.canUse(
                        store,
                        Feature.REVIEWS
                )
        ).thenReturn(true);

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
    void storefrontShouldRequireProductsFeatureBeforeLoadingCatalog() {

        Store store = new Store();
        store.setNombre("Locked Store");

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn("basic");

        doThrow(FeatureLockedException.class)
                .when(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        assertThrows(
                FeatureLockedException.class,
                () ->
                        controller.home(
                                new ConcurrentModel(),
                                request
                        )
        );

        verify(productoService, never())
                .obtenerCategorias(any());

        verify(productoService, never())
                .obtenerProductosIndexOptimizado(any());
    }

    @Test
    void productDetailShouldRequireProductsFeatureBeforeLoadingProduct() {

        Store store = new Store();
        store.setNombre("Locked Store");

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);

        doThrow(FeatureLockedException.class)
                .when(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        assertThrows(
                FeatureLockedException.class,
                () ->
                        controller.verDetalleProducto(
                                1L,
                                new ConcurrentModel(),
                                request
                        )
        );

        verify(productoService, never())
                .obtenerDetalleProducto(
                        any(),
                        any()
                );
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

        verify(featureAccessService, never())
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        verify(resenaRepository, never())
                .findByStoreOrderByEstrellasDesc(
                        any(Store.class),
                        any()
                );
    }

    @Test
    void gracias_shouldLoadOrderInsideCurrentStore() {

        Store store = new Store();
        store.setId(9L);
        store.setNombre("Stride");

        Order order = new Order();
        order.setId(77L);
        order.setStore(store);

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn("stride");

        when(orderService.getOrderById(
                77L,
                store
        )).thenReturn(order);

        when(storeThemeResolver.view(
                request,
                "gracias"
        )).thenReturn(
                "themes/stride/gracias"
        );

        Model model =
                new ConcurrentModel();

        when(request.getSession(false))
                .thenReturn(session);

        when(session.getAttribute(
                "CHECKOUT_CONFIRMATION_ORDER_ID"
        )).thenReturn(77L);

        String view =
                controller.gracias(
                        77L,
                        model,
                        request
                );

        assertThat(view)
                .isEqualTo(
                        "themes/stride/gracias"
                );

        assertThat(
                model.getAttribute("order")
        ).isSameAs(order);

        assertThat(
                model.getAttribute("store")
        ).isSameAs(store);

        verify(orderService)
                .getOrderById(
                        77L,
                        store
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
                featureAccessService.canUse(
                        store,
                        Feature.REVIEWS
                )
        ).thenReturn(true);

        when(
                resenaRepository.findByStoreOrderByEstrellasDesc(
                        any(Store.class),
                        any()
                )
        ).thenReturn(resenasPage);

        when(resenasPage.getContent())
                .thenReturn(Collections.emptyList());
    }


    @Test
    void gracias_shouldRejectRequestWithoutCheckoutSession() {

        when(request.getSession(false))
                .thenReturn(null);

        Model model =
                new ConcurrentModel();

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.gracias(
                                77L,
                                model,
                                request
                        )
                );

        assertThat(ex.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(orderService, never())
                .getOrderById(
                        any(),
                        any()
                );
    }

    @Test
    void gracias_shouldRejectDifferentOrderFromCheckoutSession() {

        when(request.getSession(false))
                .thenReturn(session);

        when(session.getAttribute(
                "CHECKOUT_CONFIRMATION_ORDER_ID"
        )).thenReturn(99L);

        Model model =
                new ConcurrentModel();

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.gracias(
                                77L,
                                model,
                                request
                        )
                );

        assertThat(ex.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(orderService, never())
                .getOrderById(
                        any(),
                        any()
                );
    }

}
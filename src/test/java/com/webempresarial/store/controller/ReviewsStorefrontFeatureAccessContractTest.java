package com.webempresarial.store.controller;

import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsStorefrontFeatureAccessContractTest {

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

    @SuppressWarnings("rawtypes")
    @Mock
    private Page reviewsPage;

    private Store store;
    private HomeController controller;

    @BeforeEach
    void setUp() {

        store = new Store();
        store.setId(10L);
        store.setNombre("Review Store");

        when(
                storeResolver.getCurrentStore(request)
        ).thenReturn(store);

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
    void lockedReviewsShouldNotBlockStorefrontHome() {

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn("basic");

        when(
                storeThemeResolver.view(
                        request,
                        "index"
                )
        ).thenReturn(
                "themes/basic/index"
        );

        when(
                productoService.obtenerCategorias(store)
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                productoService
                        .obtenerProductosIndexOptimizado(store)
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                featureAccessService.canUse(
                        store,
                        Feature.REVIEWS
                )
        ).thenReturn(false);

        Model model =
                new ConcurrentModel();

        String view =
                controller.home(
                        model,
                        request
                );

        assertThat(view)
                .isEqualTo(
                        "themes/basic/index"
                );

        assertThat(
                model.getAttribute(
                        "resenasIniciales"
                )
        )
        .isEqualTo(
                List.of()
        );

        verify(
                resenaRepository,
                never()
        )
        .findByStoreOrderByEstrellasDesc(
                any(Store.class),
                any()
        );
    }

    @Test
    void enabledReviewsShouldLoadHomeReviewsInsideCurrentStore() {

        when(storeThemeResolver.resolveTheme(store))
                .thenReturn("basic");

        when(
                storeThemeResolver.view(
                        request,
                        "index"
                )
        ).thenReturn(
                "themes/basic/index"
        );

        when(
                productoService.obtenerCategorias(store)
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                productoService
                        .obtenerProductosIndexOptimizado(store)
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                featureAccessService.canUse(
                        store,
                        Feature.REVIEWS
                )
        ).thenReturn(true);

        when(
                resenaRepository
                        .findByStoreOrderByEstrellasDesc(
                                eq(store),
                                any()
                        )
        ).thenReturn(reviewsPage);

        when(
                reviewsPage.getContent()
        ).thenReturn(
                Collections.emptyList()
        );

        controller.home(
                new ConcurrentModel(),
                request
        );

        verify(
                resenaRepository
        )
        .findByStoreOrderByEstrellasDesc(
                eq(store),
                any()
        );
    }

    @Test
    void lockedReviewsShouldReturnEmptyPublicFragment() {

        when(
                featureAccessService.canUse(
                        store,
                        Feature.REVIEWS
                )
        ).thenReturn(false);

        when(
                storeThemeResolver.fragment(
                        request,
                        "resenas"
                )
        ).thenReturn(
                "themes/basic/fragments/resenas"
        );

        Model model =
                new ConcurrentModel();

        String view =
                controller.cargarResenasFragment(
                        model,
                        request
                );

        assertThat(view)
                .isEqualTo(
                        "themes/basic/fragments/resenas :: resenas"
                );

        assertThat(
                model.getAttribute(
                        "resenas"
                )
        )
        .isEqualTo(
                List.of()
        );

        verify(
                resenaRepository,
                never()
        )
        .findByStoreOrderByEstrellasDesc(
                any(Store.class)
        );
    }

    @Test
    void enabledReviewsShouldLoadFragmentInsideCurrentStore() {

        when(
                featureAccessService.canUse(
                        store,
                        Feature.REVIEWS
                )
        ).thenReturn(true);

        when(
                resenaRepository
                        .findByStoreOrderByEstrellasDesc(store)
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                storeThemeResolver.fragment(
                        request,
                        "resenas"
                )
        ).thenReturn(
                "themes/basic/fragments/resenas"
        );

        controller.cargarResenasFragment(
                new ConcurrentModel(),
                request
        );

        verify(
                resenaRepository
        )
        .findByStoreOrderByEstrellasDesc(
                store
        );
    }
}

package com.webempresarial.store.commerce.web.admin.order;

import com.webempresarial.store.commerce.application.order.OrderAuditService;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersFeatureAccessContractTest {

    @Mock
    private OrderService orderService;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private OrderAuditService orderAuditService;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    private Store store;

    private OrdersController ordersController;
    private OrderExportController exportController;

    @BeforeEach
    void setUp() {

        store = new Store();
        store.setId(10L);

        when(
                storeResolver.getCurrentStore(request)
        )
        .thenReturn(store);

        ordersController =
                new OrdersController(
                        orderService,
                        storeResolver,
                        orderAuditService,
                        featureAccessService
                );

        exportController =
                new OrderExportController(
                        orderService,
                        storeResolver,
                        featureAccessService
                );
    }

    @Test
    void allOrderAdministrationOperationsShouldRequireOrdersFeature() {

        Order order = mock(Order.class);

        when(
                orderService.filterOrders(
                        null,
                        null,
                        null,
                        null,
                        store
                )
        )
        .thenReturn(List.of());

        when(
                orderService.getOrderByIdWithUserAndItems(
                        1L,
                        store
                )
        )
        .thenReturn(order);

        when(
                orderService.updateOrderStatus(
                        1L,
                        "SHIPPED",
                        store
                )
        )
        .thenReturn(order);

        ordersController.listarOrders(
                null,
                null,
                null,
                null,
                null,
                model,
                request
        );

        ordersController.verDetalles(
                1L,
                model,
                request
        );

        ordersController.confirmarPagoTransferencia(
                1L,
                redirectAttributes,
                request
        );

        ordersController.updateShipping(
                1L,
                "DHL",
                "TRACK-001",
                redirectAttributes,
                request
        );

        ordersController.cancelarOrden(
                1L,
                redirectAttributes,
                request
        );

        ordersController.updateOrderStatusAjax(
                1L,
                Map.of(
                        "status",
                        "SHIPPED"
                ),
                request
        );

        verify(
                featureAccessService,
                times(6)
        )
        .requireFeature(
                store,
                Feature.ORDERS
        );
    }

    @Test
    void lockedOrdersFeatureShouldStopOrderMutation() {

        doThrow(
                new FeatureLockedException(
                        Feature.ORDERS
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.ORDERS
        );

        assertThatThrownBy(
                () ->
                        ordersController.cancelarOrden(
                                1L,
                                redirectAttributes,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verifyNoInteractions(
                orderService,
                orderAuditService
        );
    }

    @Test
    void orderExportsShouldRequireOrdersFeature() {

        doThrow(
                new FeatureLockedException(
                        Feature.ORDERS
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.ORDERS
        );

        assertThatThrownBy(
                () ->
                        exportController.exportOrdersToExcel(
                                null,
                                null,
                                null,
                                null,
                                request,
                                response
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        assertThatThrownBy(
                () ->
                        exportController.exportOrdersToPDF(
                                null,
                                null,
                                null,
                                null,
                                request,
                                response
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verify(
                featureAccessService,
                times(2)
        )
        .requireFeature(
                store,
                Feature.ORDERS
        );

        verifyNoInteractions(
                orderService
        );
    }
}

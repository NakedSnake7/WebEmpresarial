package com.webempresarial.store.commerce.web.storefront.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.controller.AccountController;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.UserService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
class CustomerOrdersFeatureAccessContractTest {

    @Mock
    private UserService userService;

    @Mock
    private OrderService orderService;

    @Mock
    private StoreThemeResolver storeThemeResolver;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private UserDetails userDetails;

    @Mock
    private Model model;

    private Store store;

    private AccountController accountController;
    private PedidoClienteController pedidoClienteController;

    @BeforeEach
    void setUp() {

        store = new Store();
        store.setId(10L);

        when(
                storeResolver.getCurrentStore(request)
        )
        .thenReturn(store);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.ORDERS
                )
        )
        .thenReturn(true);

        accountController =
                new AccountController(
                        userService,
                        orderService,
                        storeThemeResolver,
                        storeResolver,
                        featureAccessService
                );

        pedidoClienteController =
                new PedidoClienteController(
                        orderService,
                        storeResolver,
                        storeThemeResolver,
                        featureAccessService
                );
    }

    @Test
    void lockedOrdersFeatureShouldStopCustomerHistoryBeforeOrderLookup() {

        when(
                featureAccessService.canUse(
                        store,
                        Feature.ORDERS
                )
        )
        .thenReturn(false);

        assertThatThrownBy(
                () ->
                        accountController.misPedidos(
                                request,
                                userDetails,
                                model
                        )
        )
        .isInstanceOfSatisfying(
                ResponseStatusException.class,
                ex ->
                        assertThat(ex.getStatusCode())
                                .isEqualTo(
                                        HttpStatus.NOT_FOUND
                                )
        );

        verifyNoInteractions(orderService);
    }

    @Test
    void lockedOrdersFeatureShouldStopCustomerDetailBeforeOrderLookup() {

        when(
                featureAccessService.canUse(
                        store,
                        Feature.ORDERS
                )
        )
        .thenReturn(false);

        assertThatThrownBy(
                () ->
                        pedidoClienteController
                                .verPedidoCliente(
                                        100L,
                                        userDetails,
                                        model,
                                        request,
                                        response
                                )
        )
        .isInstanceOfSatisfying(
                ResponseStatusException.class,
                ex ->
                        assertThat(ex.getStatusCode())
                                .isEqualTo(
                                        HttpStatus.NOT_FOUND
                                )
        );

        verifyNoInteractions(orderService);
    }

    @Test
    void customerShouldReceiveRealForbiddenStatusForAnotherCustomersOrder() {

        Order order = mock(Order.class);

        when(
                orderService.getOrderByIdWithUserAndItems(
                        100L,
                        store
                )
        )
        .thenReturn(order);

        when(order.getCustomerEmail())
                .thenReturn(
                        "otro-cliente@example.com"
                );

        when(userDetails.getUsername())
                .thenReturn(
                        "cliente@example.com"
                );

        String view =
                pedidoClienteController
                        .verPedidoCliente(
                                100L,
                                userDetails,
                                model,
                                request,
                                response
                        );

        assertThat(view)
                .isEqualTo("error/403");

        verify(response)
                .setStatus(
                        HttpServletResponse.SC_FORBIDDEN
                );

        verify(featureAccessService)
                .canUse(
                        store,
                        Feature.ORDERS
                );
    }

    @Test
    void customerShouldSeeOwnOrderDetail() {

        Order order = mock(Order.class);

        when(
                orderService.getOrderByIdWithUserAndItems(
                        100L,
                        store
                )
        )
        .thenReturn(order);

        when(order.getCustomerEmail())
                .thenReturn(
                        "cliente@example.com"
                );

        when(userDetails.getUsername())
                .thenReturn(
                        "cliente@example.com"
                );

        when(
                storeThemeResolver.view(
                        request,
                        "pedido-detalle"
                )
        )
        .thenReturn(
                "themes/stride/pedido-detalle"
        );

        String view =
                pedidoClienteController
                        .verPedidoCliente(
                                100L,
                                userDetails,
                                model,
                                request,
                                response
                        );

        assertThat(view)
                .isEqualTo(
                        "themes/stride/pedido-detalle"
                );

        verify(storeThemeResolver)
                .view(
                        request,
                        "pedido-detalle"
                );

        verify(model)
                .addAttribute(
                        "pedido",
                        order
                );

        verify(response, never())
                .setStatus(anyInt());

        verify(featureAccessService)
                .canUse(
                        store,
                        Feature.ORDERS
                );
    }
}

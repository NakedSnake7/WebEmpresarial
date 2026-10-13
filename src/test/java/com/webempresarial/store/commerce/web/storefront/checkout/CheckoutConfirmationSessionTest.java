package com.webempresarial.store.commerce.web.storefront.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.webempresarial.store.commerce.application.checkout.CheckoutService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.dto.checkout.CheckoutRequestDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.UserService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CheckoutConfirmationSessionTest {

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private UserService userService;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private CheckoutRequestDTO checkoutRequest;

    private CheckoutController controller;

    @BeforeEach
    void setUp() {
        controller =
                new CheckoutController(
                        checkoutService,
                        userService,
                        storeResolver
                );
    }

    @Test
    void checkoutShouldAuthorizeCreatedOrderInHttpSession() {

        Store store = new Store();

        Order order = new Order();
        order.setId(77L);

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);

        when(checkoutService.createOrder(
                checkoutRequest,
                store
        )).thenReturn(order);

        when(request.getSession(true))
                .thenReturn(session);

        ResponseEntity<?> response =
                controller.processCheckout(
                        checkoutRequest,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        verify(session)
                .setAttribute(
                        "CHECKOUT_CONFIRMATION_ORDER_ID",
                        77L
                );
    }
}

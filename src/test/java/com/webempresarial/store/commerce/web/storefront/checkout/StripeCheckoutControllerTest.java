package com.webempresarial.store.commerce.web.storefront.checkout;

import static org.assertj.core.api.Assertions.assertThat;  
import static org.mockito.Mockito.*;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.stripe.model.checkout.Session;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.infrastructure.checkout.payment.StripeCommerceCheckoutService;
import com.webempresarial.store.theme.StoreResolver;

@ExtendWith(MockitoExtension.class)
class StripeCheckoutControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private StripeCommerceCheckoutService stripeCheckoutService;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Order order;

    @Mock
    private Store store;

    @Mock
    private Session session;

    private StripeCheckoutController controller;

    @BeforeEach
    void setUp() {
        controller = new StripeCheckoutController(
                orderService,
                stripeCheckoutService,
                storeResolver
        );

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);
    }

    @Test
    void createStripeSession_shouldRejectPaidOrder() {

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PAID);

        ResponseEntity<?> response =
                controller.createStripeSession(
                        100L,
                        request
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(400);

        assertThat(response.getBody())
                .isEqualTo(
                        Map.of(
                                "error",
                                "La orden ya fue pagada"
                        )
                );

        verifyNoInteractions(stripeCheckoutService);
        verify(orderService, never())
                .save(any(), any());
    }

    @Test
    void createStripeSession_shouldReuseExistingActiveSession()
            throws Exception {

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(order.getStripeSessionId())
                .thenReturn("cs_existing");

        when(stripeCheckoutService.isSessionExpired(
                "cs_existing"
        )).thenReturn(false);

        when(stripeCheckoutService.getSessionUrl(
                "cs_existing"
        )).thenReturn(
                "https://checkout.stripe.com/existing"
        );

        ResponseEntity<?> response =
                controller.createStripeSession(
                        100L,
                        request
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        assertThat(response.getBody())
                .isEqualTo(
                        Map.of(
                                "url",
                                "https://checkout.stripe.com/existing"
                        )
                );

        verify(stripeCheckoutService, never())
                .createSession(any());

        verify(orderService, never())
                .save(any(), any());
    }

    @Test
    void createStripeSession_shouldReplaceExpiredSession()
            throws Exception {

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(order.getStripeSessionId())
                .thenReturn("cs_expired");

        when(stripeCheckoutService.isSessionExpired(
                "cs_expired"
        )).thenReturn(true);

        when(stripeCheckoutService.createSession(order))
                .thenReturn(session);

        when(session.getId())
                .thenReturn("cs_new");

        when(session.getUrl())
                .thenReturn(
                        "https://checkout.stripe.com/new"
                );

        ResponseEntity<?> response =
                controller.createStripeSession(
                        100L,
                        request
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        assertThat(response.getBody())
                .isEqualTo(
                        Map.of(
                                "url",
                                "https://checkout.stripe.com/new"
                        )
                );

        verify(order).setStripeSessionId(null);

        verify(order).setStripeSessionId("cs_new");

        /*
         * Una vez para persistir la eliminación
         * de la sesión expirada y otra para
         * persistir la nueva sesión.
         */
        verify(orderService, times(2))
                .save(order, store);
    }

    @Test
    void createStripeSession_shouldCreateNewSessionWhenNoneExists()
            throws Exception {

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(order.getStripeSessionId())
                .thenReturn(null);

        when(stripeCheckoutService.createSession(order))
                .thenReturn(session);

        when(session.getId())
                .thenReturn("cs_new");

        when(session.getUrl())
                .thenReturn(
                        "https://checkout.stripe.com/new"
                );

        ResponseEntity<?> response =
                controller.createStripeSession(
                        100L,
                        request
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        assertThat(response.getBody())
                .isEqualTo(
                        Map.of(
                                "url",
                                "https://checkout.stripe.com/new"
                        )
                );

        verify(order).setStripeSessionId("cs_new");

        verify(orderService)
                .save(order, store);

        verify(stripeCheckoutService)
                .createSession(order);
    }

    @Test
    void createStripeSession_shouldReturn500WhenStripeFails()
            throws Exception {

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(order.getStripeSessionId())
                .thenReturn(null);

        when(stripeCheckoutService.createSession(order))
                .thenThrow(
                        new RuntimeException(
                                "Stripe unavailable"
                        )
                );

        ResponseEntity<?> response =
                controller.createStripeSession(
                        100L,
                        request
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(500);

        assertThat(response.getBody())
                .isEqualTo(
                        Map.of(
                                "error",
                                "Error al crear sesión de pago"
                        )
                );

        verify(orderService, never())
                .save(order, store);
    }

    @Test
    void createStripeSession_shouldLoadOrderUsingCurrentStore()
            throws Exception {

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(order.getStripeSessionId())
                .thenReturn(null);

        when(stripeCheckoutService.createSession(order))
                .thenReturn(session);

        when(session.getId())
                .thenReturn("cs_123");

        when(session.getUrl())
                .thenReturn(
                        "https://checkout.stripe.com/cs_123"
                );

        controller.createStripeSession(
                100L,
                request
        );

        verify(storeResolver)
                .getCurrentStore(request);

        verify(orderService)
                .getById(100L, store);
    }
}
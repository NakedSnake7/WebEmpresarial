package com.webempresarial.store.commerce.infrastructure.checkout.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.webempresarial.store.model.Store;

import com.webempresarial.store.commerce.domain.order.Order;

class StripeCommerceCheckoutServiceTest {
	
    private StripeCommerceCheckoutService service;
    
    @BeforeEach
    void setUp() {

    	service = new StripeCommerceCheckoutService();

        ReflectionTestUtils.setField(
                service,
                "environment",
                "test"
        );
    }
    
    @Test
    void createSession_shouldNormalizeConfiguredCurrency()
            throws Exception {

        Order order = mock(Order.class);
        Store store = mock(Store.class);

        when(order.getTotal())
                .thenReturn(new BigDecimal("100.00"));

        when(order.getId())
                .thenReturn(91L);

        when(order.getStore())
                .thenReturn(store);

        when(order.getCustomerEmail())
                .thenReturn("cliente@test.com");

        when(store.getId())
                .thenReturn(12L);

        when(store.getDominio())
                .thenReturn("tenant.test.com");

        when(store.getNombre())
                .thenReturn("Tenant");

        when(store.getTheme())
                .thenReturn("WebEmpresarial");

        when(store.getCurrency())
                .thenReturn(" USD ");

        when(store.isStripeConnected())
                .thenReturn(false);

        Session stripeSession =
                mock(Session.class);

        AtomicReference<SessionCreateParams> captured =
                new AtomicReference<>();

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.create(
                            any(SessionCreateParams.class),
                            any(RequestOptions.class)
                    )
            ).thenAnswer(invocation -> {

                captured.set(
                        invocation.getArgument(0)
                );

                return stripeSession;
            });

            service.createSession(order);
        }

        String currency =
                captured.get()
                        .getLineItems()
                        .get(0)
                        .getPriceData()
                        .getCurrency();

        assertThat(currency)
                .isEqualTo("usd");
    }
    
    @Test
    void createSession_shouldNotUseStripeAccountWhenConnectedAccountIsBlank()
            throws Exception {

        Order order = mock(Order.class);
        Store store = mock(Store.class);

        when(order.getTotal())
                .thenReturn(new BigDecimal("100.00"));

        when(order.getId())
                .thenReturn(92L);

        when(order.getStore())
                .thenReturn(store);

        when(order.getCustomerEmail())
                .thenReturn("cliente@test.com");

        when(store.getId())
                .thenReturn(13L);

        when(store.getDominio())
                .thenReturn("tenant.test.com");

        when(store.getNombre())
                .thenReturn("Tenant");

        when(store.getTheme())
                .thenReturn("WebEmpresarial");

        when(store.getCurrency())
                .thenReturn("mxn");

        when(store.isStripeConnected())
                .thenReturn(true);

        when(store.getStripeConnectedAccountId())
                .thenReturn("   ");

        Session stripeSession =
                mock(Session.class);

        AtomicReference<RequestOptions> captured =
                new AtomicReference<>();

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.create(
                            any(SessionCreateParams.class),
                            any(RequestOptions.class)
                    )
            ).thenAnswer(invocation -> {

                captured.set(
                        invocation.getArgument(1)
                );

                return stripeSession;
            });

            service.createSession(order);
        }

        assertThat(captured.get().getStripeAccount())
                .isNull();
    }
    
    @Test
    void getSessionUrl_shouldReturnStripeSessionUrl()
            throws Exception {

        Session stripeSession =
                mock(Session.class);

        when(stripeSession.getUrl())
                .thenReturn(
                        "https://checkout.stripe.com/cs_test"
                );

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.retrieve("cs_test")
            ).thenReturn(stripeSession);

            String result =
                    service.getSessionUrl("cs_test");

            assertThat(result)
                    .isEqualTo(
                            "https://checkout.stripe.com/cs_test"
                    );
        }
    }
    
    @Test
    void getSessionUrl_shouldRejectSessionWithoutUrl()
            throws Exception {

        Session stripeSession =
                mock(Session.class);

        when(stripeSession.getUrl())
                .thenReturn(null);

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.retrieve("cs_invalid")
            ).thenReturn(stripeSession);

            assertThatThrownBy(() ->
                    service.getSessionUrl("cs_invalid")
            )
                    .isInstanceOf(
                            IllegalStateException.class
                    )
                    .hasMessage(
                            "Sesión Stripe no válida o expirada"
                    );
        }
    }
    @Test
    void isSessionExpired_shouldReturnTrueWhenStripeSessionIsExpired()
            throws Exception {

        Session stripeSession =
                mock(Session.class);

        when(stripeSession.getStatus())
                .thenReturn("expired");

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.retrieve("cs_expired")
            ).thenReturn(stripeSession);

            boolean result =
                    service.isSessionExpired(
                            "cs_expired"
                    );

            assertThat(result)
                    .isTrue();
        }
    }

    @Test
    void isSessionExpired_shouldReturnFalseWhenStripeSessionIsActive()
            throws Exception {

        Session stripeSession =
                mock(Session.class);

        when(stripeSession.getStatus())
                .thenReturn("open");

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.retrieve("cs_active")
            ).thenReturn(stripeSession);

            boolean result =
                    service.isSessionExpired(
                            "cs_active"
                    );

            assertThat(result)
                    .isFalse();
        }
    }

    @Test
    void isSessionExpired_shouldReturnTrueWhenStripeLookupFails()
            throws Exception {

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.retrieve("cs_error")
            ).thenThrow(
                    new RuntimeException(
                            "Stripe unavailable"
                    )
            );

            boolean result =
                    service.isSessionExpired(
                            "cs_error"
                    );

            assertThat(result)
                    .isTrue();
        }
    }
    
    
    
    @Test
    void createSession_shouldBuildEcommerceStripeContract()
            throws Exception {

        Order order = mock(Order.class);
        Store store = mock(Store.class);

        when(order.getTotal())
                .thenReturn(new BigDecimal("12.345"));

        when(order.getId())
                .thenReturn(77L);

        when(order.getStore())
                .thenReturn(store);

        when(order.getCustomerEmail())
                .thenReturn("cliente@test.com");

        when(store.getId())
                .thenReturn(5L);

        when(store.getDominio())
                .thenReturn("tienda.test.com");

        when(store.getNombre())
                .thenReturn("Tienda Test");

        when(store.getTheme())
                .thenReturn("stride");

        when(store.getCurrency())
                .thenReturn(null);

        when(store.isStripeConnected())
                .thenReturn(false);

        Session stripeSession =
                mock(Session.class);

        AtomicReference<SessionCreateParams> paramsCaptured =
                new AtomicReference<>();

        AtomicReference<RequestOptions> optionsCaptured =
                new AtomicReference<>();

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.create(
                            any(SessionCreateParams.class),
                            any(RequestOptions.class)
                    )
            ).thenAnswer(invocation -> {

                paramsCaptured.set(
                        invocation.getArgument(0)
                );

                optionsCaptured.set(
                        invocation.getArgument(1)
                );

                return stripeSession;
            });

            Session result =
                    service.createSession(order);

            assertThat(result)
                    .isSameAs(stripeSession);
        }

        SessionCreateParams params =
                paramsCaptured.get();

        RequestOptions options =
                optionsCaptured.get();

        assertThat(params)
                .isNotNull();

        assertThat(options)
                .isNotNull();

        assertThat(params.getMode())
                .isEqualTo(
                        SessionCreateParams.Mode.PAYMENT
                );

        assertThat(params.getSuccessUrl())
                .isEqualTo(
                        "https://tienda.test.com/gracias"
                        + "?session_id={CHECKOUT_SESSION_ID}"
                        + "&order_id=77"
                );

        assertThat(params.getCancelUrl())
                .isEqualTo(
                        "https://tienda.test.com"
                        + "/checkout-cancel?order_id=77"
                );

        assertThat(params.getCustomerEmail())
                .isEqualTo("cliente@test.com");

        assertThat(params.getClientReferenceId())
                .isEqualTo("ORDER-77");

        assertThat(params.getMetadata())
                .containsEntry(
                        "checkout_type",
                        "ECOMMERCE_ORDER"
                )
                .containsEntry(
                        "order_id",
                        "77"
                )
                .containsEntry(
                        "store_id",
                        "5"
                )
                .containsEntry(
                        "payment_method",
                        "STRIPE"
                )
                .containsEntry(
                        "store",
                        "Tienda Test"
                )
                .containsEntry(
                        "theme",
                        "stride"
                )
                .containsEntry(
                        "env",
                        "test"
                );

        assertThat(params.getLineItems())
                .hasSize(1);

        SessionCreateParams.LineItem line =
                params.getLineItems().get(0);

        assertThat(line.getQuantity())
                .isEqualTo(1L);

        assertThat(line.getPriceData().getCurrency())
                .isEqualTo("mxn");

        /*
         * 12.345 × 100 = 1234.5
         * HALF_UP => 1235
         */
        assertThat(line.getPriceData().getUnitAmount())
                .isEqualTo(1235L);

        assertThat(
                line.getPriceData()
                        .getProductData()
                        .getName()
        ).isEqualTo(
                "Orden #77 – Tienda Test"
        );

        assertThat(options.getIdempotencyKey())
                .isEqualTo(
                        "store_5_order_77"
                );

        assertThat(options.getStripeAccount())
                .isNull();
    }
    
    @Test
    void createSession_shouldUseConnectedStripeAccount()
            throws Exception {

        Order order = mock(Order.class);
        Store store = mock(Store.class);

        when(order.getTotal())
                .thenReturn(new BigDecimal("500.00"));

        when(order.getId())
                .thenReturn(80L);

        when(order.getStore())
                .thenReturn(store);

        when(order.getCustomerEmail())
                .thenReturn("cliente@test.com");

        when(store.getId())
                .thenReturn(9L);

        when(store.getDominio())
                .thenReturn("tenant.test.com");

        when(store.getNombre())
                .thenReturn("Tenant");

        when(store.getTheme())
                .thenReturn("WebEmpresarial");

        when(store.getCurrency())
                .thenReturn("MXN");

        when(store.isStripeConnected())
                .thenReturn(true);

        when(store.getStripeConnectedAccountId())
                .thenReturn("acct_123456");

        Session stripeSession =
                mock(Session.class);

        AtomicReference<RequestOptions> optionsCaptured =
                new AtomicReference<>();

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.create(
                            any(SessionCreateParams.class),
                            any(RequestOptions.class)
                    )
            ).thenAnswer(invocation -> {

                optionsCaptured.set(
                        invocation.getArgument(1)
                );

                return stripeSession;
            });

            service.createSession(order);
        }

        RequestOptions options =
                optionsCaptured.get();

        assertThat(options)
                .isNotNull();

        assertThat(options.getIdempotencyKey())
                .isEqualTo(
                        "store_9_order_80"
                );

        assertThat(options.getStripeAccount())
                .isEqualTo(
                        "acct_123456"
                );
    }

    @Test
    void createSession_shouldRejectNullOrder() {

        assertThatThrownBy(() ->
                service.createSession(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "La orden y su total son obligatorios"
                );
    }

    @Test
    void createSession_shouldRejectNullTotal() {

        Order order = new Order();
        order.setTotal(null);

        assertThatThrownBy(() ->
                service.createSession(order)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "La orden y su total son obligatorios"
                );
    }

    @Test
    void createSession_shouldRejectZeroTotal() {

        Order order = new Order();
        order.setTotal(BigDecimal.ZERO);

        assertThatThrownBy(() ->
                service.createSession(order)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "El total de la orden debe ser mayor a cero"
                );
    }

    @Test
    void createSession_shouldRejectNegativeTotal() {

        Order order = new Order();
        order.setTotal(
                new BigDecimal("-0.01")
        );

        assertThatThrownBy(() ->
                service.createSession(order)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "El total de la orden debe ser mayor a cero"
                );
    }
}
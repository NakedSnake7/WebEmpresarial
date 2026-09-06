package com.webempresarial.store.commerce.infrastructure.checkout.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stripe.model.checkout.Session;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class StripeCommercePaymentHandlerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private StoreRepository storeRepository;

    private StripeCommercePaymentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StripeCommercePaymentHandler(
                orderService,
                storeRepository
        );
    }

    @Test
    void handlePaidCheckout_shouldRejectNullAmountTotal() {

        Session session = mock(Session.class);
        Store store = mock(Store.class);
        Order order = mock(Order.class);

        when(storeRepository.findById(5L))
                .thenReturn(Optional.of(store));

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(order.getTotal())
                .thenReturn(new BigDecimal("500.00"));

        when(session.getAmountTotal())
                .thenReturn(null);

        assertThatThrownBy(() ->
                handler.handlePaidCheckout(
                        session,
                        ecommerceMetadata()
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "El monto pagado no coincide con la orden"
                )
                .hasMessageContaining(
                        "Esperado=50000"
                )
                .hasMessageContaining(
                        "recibido=null"
                );

        verify(orderService, never())
                .marcarOrdenComoPagada(
                        anyLong(),
                        any(),
                        any()
                );

        verify(orderService, never())
                .procesarPostPago(
                        anyLong(),
                        any()
                );
    }

    @Test
    void handlePaidCheckout_shouldRejectMissingOrderOrStoreMetadata() {

        Session session = mock(Session.class);

        Map<String, String> metadata =
                Map.of(
                        "checkout_type",
                        "ECOMMERCE_ORDER",
                        "order_id",
                        "100"
                );

        assertThatThrownBy(() ->
                handler.handlePaidCheckout(
                        session,
                        metadata
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Stripe session sin order_id o store_id"
                );

        verifyNoInteractions(storeRepository);
        verifyNoInteractions(orderService);
    }

    @Test
    void handlePaidCheckout_shouldRejectUnknownStore() {

        Session session = mock(Session.class);

        when(storeRepository.findById(5L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                handler.handlePaidCheckout(
                        session,
                        ecommerceMetadata()
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Store no encontrada"
                );

        verify(orderService, never())
                .getById(
                        anyLong(),
                        any()
                );
    }

    @Test
    void handlePaidCheckout_shouldBeIdempotentWhenOrderIsAlreadyPaid() {

        Session session = mock(Session.class);
        Store store = mock(Store.class);
        Order order = mock(Order.class);

        when(storeRepository.findById(5L))
                .thenReturn(Optional.of(store));

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PAID);

        handler.handlePaidCheckout(
                session,
                ecommerceMetadata()
        );

        verify(orderService, never())
                .marcarOrdenComoPagada(
                        anyLong(),
                        any(),
                        any()
                );

        verify(orderService, never())
                .procesarPostPago(
                        anyLong(),
                        any()
                );
    }

    @Test
    void handlePaidCheckout_shouldRejectOrderWithoutTotal() {

        Session session = mock(Session.class);
        Store store = mock(Store.class);
        Order order = mock(Order.class);

        when(storeRepository.findById(5L))
                .thenReturn(Optional.of(store));

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(order.getTotal())
                .thenReturn(null);

        assertThatThrownBy(() ->
                handler.handlePaidCheckout(
                        session,
                        ecommerceMetadata()
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "La orden no tiene un total válido"
                );

        verify(orderService, never())
                .marcarOrdenComoPagada(
                        anyLong(),
                        any(),
                        any()
                );

        verify(orderService, never())
                .procesarPostPago(
                        anyLong(),
                        any()
                );
    }

    @Test
    void handlePaidCheckout_shouldRejectAmountMismatch() {

        Session session = mock(Session.class);
        Store store = mock(Store.class);
        Order order = mock(Order.class);

        when(storeRepository.findById(5L))
                .thenReturn(Optional.of(store));

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        when(order.getTotal())
                .thenReturn(
                        new BigDecimal("1250.00")
                );

        when(session.getAmountTotal())
                .thenReturn(124999L);

        assertThatThrownBy(() ->
                handler.handlePaidCheckout(
                        session,
                        ecommerceMetadata()
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "El monto pagado no coincide con la orden"
                )
                .hasMessageContaining(
                        "Esperado=125000"
                )
                .hasMessageContaining(
                        "recibido=124999"
                );

        verify(orderService, never())
                .marcarOrdenComoPagada(
                        anyLong(),
                        any(),
                        any()
                );

        verify(orderService, never())
                .procesarPostPago(
                        anyLong(),
                        any()
                );
    }

    @Test
    void handlePaidCheckout_shouldConfirmPaymentAndProcessPostPayment() {

        Session session = mock(Session.class);
        Store store = mock(Store.class);
        Order order = mock(Order.class);

        when(storeRepository.findById(5L))
                .thenReturn(Optional.of(store));

        when(orderService.getById(100L, store))
                .thenReturn(order);

        when(order.getPaymentStatus())
                .thenReturn(PaymentStatus.PENDING);

        /*
         * 12.345 x 100 = 1234.5
         * HALF_UP => 1235
         */
        when(order.getTotal())
                .thenReturn(
                        new BigDecimal("12.345")
                );

        when(session.getAmountTotal())
                .thenReturn(1235L);

        when(session.getPaymentIntent())
                .thenReturn("pi_123456");

        handler.handlePaidCheckout(
                session,
                ecommerceMetadata()
        );

        verify(orderService)
                .marcarOrdenComoPagada(
                        100L,
                        "pi_123456",
                        store
                );

        verify(orderService)
                .procesarPostPago(
                        100L,
                        store
                );

        var inOrder = inOrder(orderService);

        inOrder.verify(orderService)
                .marcarOrdenComoPagada(
                        100L,
                        "pi_123456",
                        store
                );

        inOrder.verify(orderService)
                .procesarPostPago(
                        100L,
                        store
                );
    }

    private Map<String, String> ecommerceMetadata() {
        return Map.of(
                "checkout_type",
                "ECOMMERCE_ORDER",
                "order_id",
                "100",
                "store_id",
                "5"
        );
    }
}
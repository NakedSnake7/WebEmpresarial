package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderAuditAction;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.OrderTransition;
import com.webempresarial.store.commerce.domain.order.OrderTransitionContext;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.commerce.application.order.OrderAuditService;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.application.order.OrderStateMachine;

class OrderStateMachineTest {

    @Mock
    private OrderAuditService orderAuditService;

    private OrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        stateMachine = new OrderStateMachine(orderAuditService);
    }

    @Test
    void shouldRejectNullOrder() {
        assertThatThrownBy(() ->
                stateMachine.transition(
                        null,
                        OrderTransition.PAYMENT_CONFIRMED,
                        OrderTransitionContext.empty()
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("La orden es obligatoria");
    }

    @Test
    void shouldRejectNullTransition() {
        Order order = new Order();

        assertThatThrownBy(() ->
                stateMachine.transition(
                        order,
                        null,
                        OrderTransitionContext.empty()
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("La transición es obligatoria");
    }

    @Test
    void shouldConfirmPayment() {
        Order order = new Order();

        stateMachine.transition(
                order,
                OrderTransition.PAYMENT_CONFIRMED,
                OrderTransitionContext.payment("pi_123")
        );

        assertThat(order.getPaymentStatus())
                .isEqualTo(PaymentStatus.PAID);

        assertThat(order.getOrderStatus())
                .isEqualTo(OrderStatus.PAID_PENDING_STOCK);

        assertThat(order.getPaymentIntentId())
                .isEqualTo("pi_123");

        verify(orderAuditService).record(
                order,
                OrderAuditAction.PAYMENT_CONFIRMED,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                "Pago confirmado. paymentIntentId=pi_123"
        );
    }

    @Test
    void shouldUseEmptyContextWhenContextIsNull() {
        Order order = new Order();

        stateMachine.transition(
                order,
                OrderTransition.PAYMENT_CONFIRMED,
                null
        );

        assertThat(order.getPaymentStatus())
                .isEqualTo(PaymentStatus.PAID);

        verify(orderAuditService).record(
                order,
                OrderAuditAction.PAYMENT_CONFIRMED,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                "Pago confirmado manualmente"
        );
    }

    @Test
    void shouldNotAuditIdempotentPaymentConfirmation() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PAID_PENDING_STOCK);

        stateMachine.transition(
                order,
                OrderTransition.PAYMENT_CONFIRMED,
                OrderTransitionContext.payment("pi_duplicate")
        );

        verify(orderAuditService, never()).record(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldConfirmStock() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PAID_PENDING_STOCK);

        stateMachine.transition(
                order,
                OrderTransition.STOCK_CONFIRMED,
                OrderTransitionContext.empty()
        );

        assertThat(order.getOrderStatus())
                .isEqualTo(OrderStatus.PROCESSED);

        verify(orderAuditService).record(
                order,
                OrderAuditAction.STOCK_CONFIRMED,
                OrderStatus.PAID_PENDING_STOCK,
                PaymentStatus.PAID,
                "Stock confirmado y orden procesada"
        );
    }

    @Test
    void shouldNotAuditRepeatedStockConfirmation() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSED);

        stateMachine.transition(
                order,
                OrderTransition.STOCK_CONFIRMED,
                OrderTransitionContext.empty()
        );

        verify(orderAuditService, never()).record(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldMarkStockFailure() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSED);

        stateMachine.transition(
                order,
                OrderTransition.STOCK_FAILED,
                OrderTransitionContext.empty()
        );

        assertThat(order.getOrderStatus())
                .isEqualTo(OrderStatus.PAID_PENDING_STOCK);

        verify(orderAuditService).record(
                order,
                OrderAuditAction.STOCK_FAILED,
                OrderStatus.PROCESSED,
                PaymentStatus.PAID,
                "No fue posible confirmar el stock"
        );
    }

    @Test
    void shouldShipProcessedOrder() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSED);

        stateMachine.transition(
                order,
                OrderTransition.SHIPPED,
                OrderTransitionContext.shipping(
                        "TRACK-001",
                        "DHL"
                )
        );

        assertThat(order.getOrderStatus())
                .isEqualTo(OrderStatus.SHIPPED);

        assertThat(order.getTrackingNumber())
                .isEqualTo("TRACK-001");

        assertThat(order.getCarrier())
                .isEqualTo("DHL");

        verify(orderAuditService).record(
                order,
                OrderAuditAction.SHIPPING_UPDATED,
                OrderStatus.PROCESSED,
                PaymentStatus.PAID,
                "Envío registrado. carrier=DHL, tracking=TRACK-001"
        );
    }

    @Test
    void shouldRejectShippingWithoutTracking() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSED);

        assertThatThrownBy(() ->
                stateMachine.transition(
                        order,
                        OrderTransition.SHIPPED,
                        OrderTransitionContext.shipping(
                                null,
                                "DHL"
                        )
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Tracking requerido");

        verify(orderAuditService, never()).record(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldDeliverShippedOrder() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.SHIPPED);

        stateMachine.transition(
                order,
                OrderTransition.DELIVERED,
                OrderTransitionContext.empty()
        );

        assertThat(order.getOrderStatus())
                .isEqualTo(OrderStatus.DELIVERED);

        verify(orderAuditService).record(
                order,
                OrderAuditAction.ORDER_DELIVERED,
                OrderStatus.SHIPPED,
                PaymentStatus.PAID,
                "Orden marcada como entregada"
        );
    }

    @Test
    void shouldCancelUnpaidOrder() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.CREATED);

        stateMachine.transition(
                order,
                OrderTransition.CANCELLED,
                OrderTransitionContext.empty()
        );

        assertThat(order.getOrderStatus())
                .isEqualTo(OrderStatus.CANCELLED);

        verify(orderAuditService).record(
                order,
                OrderAuditAction.ORDER_CANCELLED,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                "Orden cancelada manualmente"
        );
    }

    @Test
    void shouldRejectCancellationOfPaidOrder() {
        Order order = new Order();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PAID_PENDING_STOCK);

        assertThatThrownBy(() ->
                stateMachine.transition(
                        order,
                        OrderTransition.CANCELLED,
                        OrderTransitionContext.empty()
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
                "Una orden pagada no puede cancelarse directamente"
        );

        verify(orderAuditService, never()).record(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldExpireEligibleTransferOrder() {
        Order order = new Order();

        order.setPaymentMethod(
                Order.PaymentMethod.TRANSFER
        );

        order.setPaymentStatus(
                PaymentStatus.PENDING
        );

        order.setOrderStatus(
                OrderStatus.CREATED
        );

        order.setOrderDate(
                java.time.LocalDateTime.now()
                        .minusHours(25)
        );

        stateMachine.transition(
                order,
                OrderTransition.EXPIRED,
                OrderTransitionContext.empty()
        );

        assertThat(order.getOrderStatus())
                .isEqualTo(OrderStatus.CANCELLED);

        assertThat(order.getPaymentStatus())
                .isEqualTo(PaymentStatus.EXPIRED);

        verify(orderAuditService).record(
                order,
                OrderAuditAction.ORDER_EXPIRED,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                "Orden expirada automáticamente"
        );
    }

    @Test
    void shouldRejectExpirationOfIneligibleOrder() {
        Order order = new Order();

        order.setPaymentMethod(
                Order.PaymentMethod.STRIPE
        );

        order.setPaymentStatus(
                PaymentStatus.PENDING
        );

        order.setOrderStatus(
                OrderStatus.CREATED
        );

        order.setOrderDate(
                java.time.LocalDateTime.now()
                        .minusHours(25)
        );

        assertThatThrownBy(() ->
                stateMachine.transition(
                        order,
                        OrderTransition.EXPIRED,
                        OrderTransitionContext.empty()
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("La orden no puede expirar");

        verify(orderAuditService, never()).record(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldNotAuditRepeatedExpiration() {
        Order order = new Order();

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.EXPIRED);

        stateMachine.transition(
                order,
                OrderTransition.EXPIRED,
                OrderTransitionContext.empty()
        );

        verify(orderAuditService, never()).record(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
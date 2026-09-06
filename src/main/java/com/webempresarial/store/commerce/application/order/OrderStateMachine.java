package com.webempresarial.store.commerce.application.order;

import org.springframework.stereotype.Component;

import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderAuditAction;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.OrderTransition;
import com.webempresarial.store.commerce.domain.order.OrderTransitionContext;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;

@Component
public class OrderStateMachine {
	
	
	private final OrderAuditService orderAuditService;
	
	public OrderStateMachine(
	        OrderAuditService orderAuditService
	) {
	    this.orderAuditService = orderAuditService;
	}
	
	

	public void transition(
	        Order order,
	        OrderTransition transition,
	        OrderTransitionContext context
	) {
	    if (order == null) {
	        throw new IllegalArgumentException(
	                "La orden es obligatoria"
	        );
	    }

	    if (transition == null) {
	        throw new IllegalArgumentException(
	                "La transición es obligatoria"
	        );
	    }

	    OrderTransitionContext safeContext =
	            context != null
	                    ? context
	                    : OrderTransitionContext.empty();

	    OrderStatus previousOrderStatus =
	            order.getOrderStatus();

	    PaymentStatus previousPaymentStatus =
	            order.getPaymentStatus();

	    switch (transition) {
	        case PAYMENT_CONFIRMED ->
	                confirmPayment(
	                        order,
	                        safeContext.paymentIntentId()
	                );

	        case STOCK_CONFIRMED ->
	                confirmStock(order);

	        case STOCK_FAILED ->
	                markStockFailure(order);

	        case SHIPPED ->
	                ship(
	                        order,
	                        safeContext.trackingNumber(),
	                        safeContext.carrier()
	                );

	        case DELIVERED ->
	                deliver(order);

	        case CANCELLED ->
	                cancel(order);

	        case EXPIRED ->
	                expire(order);
	    }

	    boolean changed =
	            previousOrderStatus != order.getOrderStatus()
	            || previousPaymentStatus != order.getPaymentStatus();

	    if (changed) {
	        orderAuditService.record(
	                order,
	                mapAuditAction(transition),
	                previousOrderStatus,
	                previousPaymentStatus,
	                buildReason(transition, safeContext)
	        );
	    }
	}
	
	
	private OrderAuditAction mapAuditAction(
	        OrderTransition transition
	) {
	    return switch (transition) {
	        case PAYMENT_CONFIRMED ->
	                OrderAuditAction.PAYMENT_CONFIRMED;

	        case STOCK_CONFIRMED ->
	                OrderAuditAction.STOCK_CONFIRMED;

	        case STOCK_FAILED ->
	                OrderAuditAction.STOCK_FAILED;

	        case SHIPPED ->
	                OrderAuditAction.SHIPPING_UPDATED;

	        case DELIVERED ->
	                OrderAuditAction.ORDER_DELIVERED;

	        case CANCELLED ->
	                OrderAuditAction.ORDER_CANCELLED;

	        case EXPIRED ->
	                OrderAuditAction.ORDER_EXPIRED;
	    };
	}

	private String buildReason(
	        OrderTransition transition,
	        OrderTransitionContext context
	) {
	    return switch (transition) {
	        case PAYMENT_CONFIRMED ->
	                context.paymentIntentId() != null
	                        ? "Pago confirmado. paymentIntentId="
	                            + context.paymentIntentId()
	                        : "Pago confirmado manualmente";

	        case STOCK_CONFIRMED ->
	                "Stock confirmado y orden procesada";

	        case STOCK_FAILED ->
	                "No fue posible confirmar el stock";

	        case SHIPPED ->
	                "Envío registrado. carrier="
	                        + context.carrier()
	                        + ", tracking="
	                        + context.trackingNumber();

	        case DELIVERED ->
	                "Orden marcada como entregada";

	        case CANCELLED ->
	                "Orden cancelada manualmente";

	        case EXPIRED ->
	                "Orden expirada automáticamente";
	    };
	}
	

    private void confirmPayment(
            Order order,
            String paymentIntentId
    ) {
        /*
         * markAsPaid() ya es idempotente:
         * si la orden está pagada, no vuelve a modificarla.
         */
        order.markAsPaid(paymentIntentId);
    }

    private void confirmStock(Order order) {

        if (order.getOrderStatus() == OrderStatus.PROCESSED) {
            return;
        }

        order.markAsProcessed();
    }

    private void markStockFailure(Order order) {

        if (order.getOrderStatus()
                == OrderStatus.PAID_PENDING_STOCK) {
            return;
        }

        order.markAsPendingStock();
    }

    private void ship(
            Order order,
            String trackingNumber,
            String carrier
    ) {
        if (order.getOrderStatus() == OrderStatus.SHIPPED) {
            return;
        }

        order.markAsShipped(
                trackingNumber,
                carrier
        );
    }

    private void deliver(Order order) {

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            return;
        }

        order.changeStatus(
                OrderStatus.DELIVERED
        );
    }

    private void cancel(Order order) {

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (order.isPaid()) {
            throw new IllegalStateException(
                    "Una orden pagada no puede cancelarse directamente"
            );
        }

        order.changeStatus(
                OrderStatus.CANCELLED
        );
    }

    private void expire(Order order) {

        if (order.getOrderStatus() == OrderStatus.CANCELLED
                && order.getPaymentStatus()
                        == com.webempresarial.store.commerce.domain.order.PaymentStatus.EXPIRED) {
            return;
        }

        order.markAsExpired();
    }
}
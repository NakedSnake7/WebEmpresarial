package com.webempresarial.store.commerce.application.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.OrderTransition;
import com.webempresarial.store.commerce.domain.order.OrderTransitionContext;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderRepository;
import com.webempresarial.store.exceptions.OrderNotFoundException;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StockService;

@Service
public class OrderPostPaymentTransactionService {

    private final OrderRepository orderRepository;
    private final StockService stockService;
    private final OrderStateMachine orderStateMachine;

    public OrderPostPaymentTransactionService(
            OrderRepository orderRepository,
            StockService stockService,
            OrderStateMachine orderStateMachine
    ) {
        this.orderRepository = orderRepository;
        this.stockService = stockService;
        this.orderStateMachine = orderStateMachine;
    }

    @Transactional
    public void processStock(
            Long orderId,
            Store store
    ) {

        Order order = getFullOrderByIdForUpdate(
                orderId,
                store
        );

        if (!order.isPaid()) {
            throw new IllegalStateException(
                    "No puedes procesar una orden no pagada"
            );
        }

        if (!order.isStockReduced()) {
            stockService.descontarStock(
                    order,
                    store
            );
        }

        if (order.getOrderStatus()
                == OrderStatus.PAID_PENDING_STOCK) {

            orderStateMachine.transition(
                    order,
                    OrderTransition.STOCK_CONFIRMED,
                    OrderTransitionContext.empty()
            );
        }
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markStockFailed(
            Long orderId,
            Store store
    ) {

        Order order = getFullOrderByIdForUpdate(
                orderId,
                store
        );

        if (!order.isPaid()) {
            throw new IllegalStateException(
                    "No puedes marcar fallo de stock "
                            + "en una orden no pagada"
            );
        }

        if (order.getOrderStatus()
                == OrderStatus.PAID_PENDING_STOCK) {
            return;
        }

        orderStateMachine.transition(
                order,
                OrderTransition.STOCK_FAILED,
                OrderTransitionContext.empty()
        );
    }

    @Transactional(readOnly = true)
    public Order getProcessedOrder(
            Long orderId,
            Store store
    ) {
        return orderRepository
                .findByIdFullAndStore(
                        orderId,
                        store
                )
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Orden no encontrada"
                        )
                );
    }

    private Order getFullOrderByIdForUpdate(
            Long orderId,
            Store store
    ) {
        return orderRepository
                .findByIdFullForUpdateAndStore(
                        orderId,
                        store
                )
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Orden no encontrada"
                        )
                );
    }
}
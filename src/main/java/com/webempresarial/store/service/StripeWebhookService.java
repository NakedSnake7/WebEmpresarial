package com.webempresarial.store.service;

import com.stripe.model.checkout.Session;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StripeWebhookService {

    private final OrderService orderService;
    private final StoreRepository storeRepository;

    public StripeWebhookService(
            OrderService orderService,
            StoreRepository storeRepository
    ) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public void procesarCheckoutCompleted(Session session) {

        if (!"paid".equals(session.getPaymentStatus())) {
            return;
        }

        String orderIdMeta = session.getMetadata().get("order_id");
        String storeIdMeta = session.getMetadata().get("store_id");

        if (orderIdMeta == null || storeIdMeta == null) {
            throw new IllegalStateException(
                    "Stripe session sin order_id o store_id"
            );
        }

        Long orderId = Long.valueOf(orderIdMeta);
        Long storeId = Long.valueOf(storeIdMeta);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new IllegalStateException("Store no encontrada")
                );

        Order order = orderService.getById(orderId, store);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        Long expected = Math.round(order.getTotal() * 100);

        if (!session.getAmountTotal().equals(expected)) {
            throw new IllegalStateException(
                    "El monto pagado no coincide con la orden"
            );
        }

        orderService.marcarOrdenComoPagada(
                orderId,
                session.getPaymentIntent(),
                store
        );

        orderService.procesarPostPago(
                orderId,
                store
        );
    }
}
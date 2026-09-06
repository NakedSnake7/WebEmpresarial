package com.webempresarial.store.commerce.infrastructure.checkout.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stripe.model.checkout.Session;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

@Service
public class StripeCommercePaymentHandler {

    private final OrderService orderService;
    private final StoreRepository storeRepository;

    public StripeCommercePaymentHandler(
            OrderService orderService,
            StoreRepository storeRepository
    ) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
    }

    public void handlePaidCheckout(
            Session session,
            Map<String, String> metadata
    ) {

        String orderIdMeta =
                metadata.get("order_id");

        String storeIdMeta =
                metadata.get("store_id");

        if (orderIdMeta == null
                || storeIdMeta == null) {

            throw new IllegalStateException(
                    "Stripe session sin order_id o store_id"
            );
        }

        Long orderId =
                Long.valueOf(orderIdMeta);

        Long storeId =
                Long.valueOf(storeIdMeta);

        Store store =
                storeRepository.findById(storeId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Store no encontrada"
                                )
                        );

        Order order =
                orderService.getById(
                        orderId,
                        store
                );

        if (order.getPaymentStatus()
                == PaymentStatus.PAID) {
            return;
        }

        if (order.getTotal() == null) {
            throw new IllegalStateException(
                    "La orden no tiene un total válido"
            );
        }

        Long expected =
                order.getTotal()
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .setScale(
                                0,
                                RoundingMode.HALF_UP
                        )
                        .longValueExact();

        Long amountTotal =
                session.getAmountTotal();

        if (amountTotal == null
                || !amountTotal.equals(expected)) {

            throw new IllegalStateException(
                    "El monto pagado no coincide con la orden. "
                            + "Esperado="
                            + expected
                            + ", recibido="
                            + amountTotal
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
package com.webempresarial.store.service;

import java.util.Map;

import com.stripe.model.checkout.Session;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StripeWebhookService {

    private final OrderService orderService;
    private final StoreRepository storeRepository;
    private final ProvisioningService provisioningService;

    public StripeWebhookService(
            OrderService orderService,
            StoreRepository storeRepository,
            ProvisioningService provisioningService
    ) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
        this.provisioningService = provisioningService;
    }

    @Transactional
    public void procesarCheckoutCompleted(Session session) {

        Map<String, String> metadata = session.getMetadata();

        if (metadata == null) {
            return;
        }

        String checkoutType = metadata.get("checkout_type");

        if ("SAAS_SUBSCRIPTION".equalsIgnoreCase(checkoutType)) {
            procesarSaasSubscription(session, metadata);
            return;
        }

        if ("ECOMMERCE_ORDER".equalsIgnoreCase(checkoutType)) {
            procesarEcommerceOrder(session, metadata);
        }
    }

    private void procesarSaasSubscription(
            Session session,
            Map<String, String> metadata
    ) {
        String companyName = metadata.get("companyName");
        String domain = metadata.get("domain");
        String ownerName = metadata.get("ownerName");
        String email = metadata.get("email");
        String planValue = metadata.get("plan");

        if (companyName == null || domain == null || ownerName == null || email == null || planValue == null) {
            throw new IllegalStateException("Stripe session SaaS sin metadata completa");
        }

        StorePlan plan = StorePlan.valueOf(planValue);

        provisioningService.provisionStoreFromCheckout(
                companyName,
                domain,
                ownerName,
                email,
                plan,
                session.getCustomer(),
                session.getSubscription(),
                null
        );
    }

    private void procesarEcommerceOrder(
            Session session,
            Map<String, String> metadata
    ) {
        if (!"paid".equals(session.getPaymentStatus())) {
            return;
        }

        String orderIdMeta = metadata.get("order_id");
        String storeIdMeta = metadata.get("store_id");

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
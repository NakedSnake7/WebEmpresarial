package com.webempresarial.store.service;

import java.util.Map; 

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.webempresarial.store.entity.StripeWebhookEvent;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.StripeWebhookEventStatus;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StripeWebhookEventRepository;

@Service
public class StripeWebhookService {

    private final OrderService orderService;
    private final StoreRepository storeRepository;
    private final ProvisioningService provisioningService;
    private final SubscriptionService subscriptionService;
    private final StripeWebhookEventRepository webhookEventRepository;
    private final StripePlanMapper stripePlanMapper;

    public StripeWebhookService(
            OrderService orderService,
            StoreRepository storeRepository,
            ProvisioningService provisioningService,
            SubscriptionService subscriptionService,
            StripeWebhookEventRepository webhookEventRepository,
            StripePlanMapper stripePlanMapper
    ) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
        this.provisioningService = provisioningService;
        this.subscriptionService = subscriptionService;
        this.webhookEventRepository = webhookEventRepository;
        this.stripePlanMapper = stripePlanMapper;
    }

    @Transactional
    public void handle(Event event) {

        String stripeEventId = event.getId();

        StripeWebhookEvent webhookEvent = webhookEventRepository
                .findByStripeEventId(stripeEventId)
                .orElse(null);

        if (webhookEvent != null && webhookEvent.isProcessed()) {
            return;
        }

        if (webhookEvent == null) {
            webhookEvent = new StripeWebhookEvent();
            webhookEvent.setStripeEventId(stripeEventId);
            webhookEvent.setEventType(event.getType());
            webhookEvent.setStatus(StripeWebhookEventStatus.PROCESSING);
            webhookEvent = webhookEventRepository.save(webhookEvent);
        }

        try {
            switch (event.getType()) {

                case "checkout.session.completed" ->
                        handleCheckoutSessionCompleted(event);
                        
                case "customer.subscription.created" ->
                handleSubscriptionUpdated(event);

                case "customer.subscription.updated" ->
                        handleSubscriptionUpdated(event);

                case "customer.subscription.deleted" ->
                        handleSubscriptionDeleted(event);

                case "invoice.payment_failed" ->
                        handleInvoicePaymentFailed(event);

                case "invoice.paid" ->
                        handleInvoicePaid(event);

                default -> {
                    // Evento ignorado, pero marcado como procesado
                }
            }

            webhookEvent.markProcessed();

        } catch (Exception e) {
            webhookEvent.markFailed(e);
            throw e;
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {

        Session session = deserializeEventObject(event, Session.class);

        if (session == null) {
            throw new IllegalStateException("No se pudo deserializar checkout.session.completed");
        }

        procesarCheckoutCompleted(session);
    }

    private void handleSubscriptionUpdated(Event event) {

        Subscription stripeSubscription =
                deserializeEventObject(event, Subscription.class);

        if (stripeSubscription == null) {
            throw new IllegalStateException("No se pudo deserializar customer.subscription.updated");
        }

        subscriptionService.syncStripeSubscriptionUpdated(stripeSubscription);
    }

    private void handleSubscriptionDeleted(Event event) {

        Subscription stripeSubscription =
                deserializeEventObject(event, Subscription.class);

        if (stripeSubscription == null) {
            throw new IllegalStateException("No se pudo deserializar customer.subscription.deleted");
        }

        subscriptionService.cancelByStripeSubscriptionId(
                stripeSubscription.getId()
        );
    }

    private void handleInvoicePaymentFailed(Event event) {

        Invoice invoice = deserializeEventObject(event, Invoice.class);

        if (invoice == null || invoice.getSubscription() == null) {
            throw new IllegalStateException("Invoice sin subscription");
        }

        subscriptionService.markPastDue(
                invoice.getSubscription()
        );
    }

    private void handleInvoicePaid(Event event) {

        Invoice invoice = deserializeEventObject(event, Invoice.class);

        if (invoice == null || invoice.getSubscription() == null) {
            return;
        }

        subscriptionService.registerSuccessfulPayment(
                invoice.getSubscription()
        );
    }
    @Transactional
    public void procesarCheckoutCompleted(Session session) {

        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            return;
        }

        Map<String, String> metadata = session.getMetadata();

        if (metadata == null) {
            return;
        }

        String checkoutType = metadata.get("checkout_type");

        switch (checkoutType) {

            case "SAAS_SUBSCRIPTION" ->
                    procesarNuevaTiendaSaas(session, metadata);

            case "SAAS_SUBSCRIPTION_EXISTING_STORE" ->
                    procesarUpgradeSaas(session, metadata);

            case "ECOMMERCE_ORDER" ->
                    procesarEcommerceOrder(session, metadata);

            default -> {
                // checkout desconocido
            }
        }
    }

    private void procesarNuevaTiendaSaas(
            Session session,
            Map<String, String> metadata
    ) {
        String companyName = metadata.get("companyName");
        String domain = metadata.get("domain");
        String ownerName = metadata.get("ownerName");
        String email = metadata.get("email");
        String planValue = metadata.get("plan");
        String stripePriceId = metadata.get("stripe_price_id");
        
        if (companyName == null ||
                domain == null ||
                ownerName == null ||
                email == null ||
                planValue == null ||
                stripePriceId == null) {
            throw new IllegalStateException("Stripe session SaaS sin metadata completa");
        }

        StorePlan plan = StorePlan.valueOf(planValue);
        
        if (!stripePlanMapper.matches(plan, stripePriceId)) {
            throw new IllegalStateException("Stripe priceId no corresponde al plan recibido");
        }

        provisioningService.provisionStoreFromCheckout(
                companyName,
                domain,
                ownerName,
                email,
                plan,
                session.getCustomer(),
                session.getSubscription(),
                stripePriceId
        );
    }
    private void procesarUpgradeSaas(
            Session session,
            Map<String, String> metadata
    ) {
        String storeIdValue = metadata.get("store_id");
        String planValue = metadata.get("plan");
        String stripePriceId = metadata.get("stripe_price_id");

        if (storeIdValue == null ||
                planValue == null ||
                stripePriceId == null) {
            throw new IllegalStateException("Stripe session upgrade SaaS sin metadata completa");
        }

        StorePlan plan = StorePlan.valueOf(planValue);

        if (!stripePlanMapper.matches(plan, stripePriceId)) {
            throw new IllegalStateException("Stripe priceId no corresponde al plan recibido");
        }

        Long storeId = Long.valueOf(storeIdValue);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("Store no encontrada para upgrade SaaS"));

        subscriptionService.activate(
                store,
                plan,
                session.getCustomer(),
                session.getSubscription(),
                stripePriceId
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
            throw new IllegalStateException("Stripe session sin order_id o store_id");
        }

        Long orderId = Long.valueOf(orderIdMeta);
        Long storeId = Long.valueOf(storeIdMeta);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("Store no encontrada"));

        Order order = orderService.getById(orderId, store);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        Long expected = Math.round(order.getTotal() * 100);

        if (!session.getAmountTotal().equals(expected)) {
            throw new IllegalStateException("El monto pagado no coincide con la orden");
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

    private <T> T deserializeEventObject(
            Event event,
            Class<T> clazz
    ) {
    	var optionalObject =
    	        event.getDataObjectDeserializer().getObject();

        if (optionalObject.isPresent()) {
            Object obj = optionalObject.get();

            if (clazz.isInstance(obj)) {
                return clazz.cast(obj);
            }
        }

        String rawJson =
                event.getDataObjectDeserializer().getRawJson();

        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }

        if (clazz.equals(Session.class)) {
            return clazz.cast(Session.GSON.fromJson(rawJson, Session.class));
        }

        if (clazz.equals(Subscription.class)) {
            return clazz.cast(Subscription.GSON.fromJson(rawJson, Subscription.class));
        }

        if (clazz.equals(Invoice.class)) {
            return clazz.cast(Invoice.GSON.fromJson(rawJson, Invoice.class));
        }

        return null;
    }
}
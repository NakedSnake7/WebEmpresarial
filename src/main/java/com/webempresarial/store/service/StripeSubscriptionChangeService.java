package com.webempresarial.store.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.SubscriptionItem;
import com.stripe.net.RequestOptions;
import com.stripe.param.SubscriptionUpdateParams;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.PlanChangeResult;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.SubscriptionRepository;

import jakarta.transaction.Transactional;

@Service
public class StripeSubscriptionChangeService {

    private final SubscriptionRepository subscriptionRepository;
    private final StripePlanMapper stripePlanMapper;

    public StripeSubscriptionChangeService(
            SubscriptionRepository subscriptionRepository,
            StripePlanMapper stripePlanMapper
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.stripePlanMapper = stripePlanMapper;
    }
    
    public boolean isReusable(
            Subscription localSubscription
    ) throws StripeException {

        if (localSubscription == null) {
            return false;
        }

        String stripeSubscriptionId =
                localSubscription.getStripeSubscriptionId();

        if (stripeSubscriptionId == null
                || stripeSubscriptionId.isBlank()) {
            return false;
        }

        com.stripe.model.Subscription stripeSubscription =
                com.stripe.model.Subscription.retrieve(
                        stripeSubscriptionId
                );

        String status = stripeSubscription.getStatus();

        if (status == null) {
            return false;
        }

        return switch (status.toLowerCase()) {
            case "active",
                 "trialing",
                 "past_due",
                 "unpaid" -> true;

            case "canceled",
                 "cancelled",
                 "incomplete_expired" -> false;

            default -> false;
        };
    }

    @Transactional
    public PlanChangeResult changePlan(
            Store store,
            StorePlan targetPlan
    ) throws StripeException {

        if (targetPlan == null) {
            throw new IllegalArgumentException(
                    "El plan destino es obligatorio"
            );
        }

        Subscription localSubscription =
                getLocalSubscription(store);

        StorePlan currentPlan = localSubscription.getPlan();

        if (targetPlan == currentPlan) {
            return PlanChangeResult.NO_CHANGE;
        }

        if (targetPlan.isHigherThan(currentPlan)) {
            upgradeImmediately(
                    store,
                    localSubscription,
                    targetPlan
            );

            return PlanChangeResult.UPGRADED;
        }

        scheduleDowngrade(
                store,
                localSubscription,
                targetPlan
        );

        return PlanChangeResult.DOWNGRADE_SCHEDULED;
    }
    
    private Subscription getLocalSubscription(Store store) {

        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        Subscription subscription = subscriptionRepository
                .findByStoreId(store.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "La tienda no tiene suscripción registrada"
                        )
                );

        if (subscription.getStripeSubscriptionId() == null
                || subscription.getStripeSubscriptionId().isBlank()) {
            throw new IllegalStateException(
                    "La tienda no tiene una suscripción asociada en Stripe"
            );
        }

        return subscription;
    }
   
    private void upgradeImmediately(
            Store store,
            Subscription localSubscription,
            StorePlan targetPlan
    ) throws StripeException {

        String targetPriceId =
                stripePlanMapper.getPriceId(targetPlan);

        com.stripe.model.Subscription stripeSubscription =
                com.stripe.model.Subscription.retrieve(
                        localSubscription.getStripeSubscriptionId()
                );

        SubscriptionItem currentItem =
                getSingleSubscriptionItem(stripeSubscription);

        if (currentItem.getPrice() != null
                && targetPriceId.equals(
                        currentItem.getPrice().getId()
                )) {
            return;
        }

        SubscriptionUpdateParams.Item updatedItem =
                SubscriptionUpdateParams.Item.builder()
                        .setId(currentItem.getId())
                        .setPrice(targetPriceId)
                        .setQuantity(1L)
                        .build();

        SubscriptionUpdateParams params =
                SubscriptionUpdateParams.builder()
                        .addItem(updatedItem)
                        .setProrationBehavior(
                                SubscriptionUpdateParams
                                        .ProrationBehavior
                                        .ALWAYS_INVOICE
                        )
                        .putMetadata(
                                "store_id",
                                store.getId().toString()
                        )
                        .putMetadata(
                                "plan",
                                targetPlan.name()
                        )
                        .putMetadata(
                                "stripe_price_id",
                                targetPriceId
                        )
                        .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(
                        "upgrade_"
                                + store.getId()
                                + "_"
                                + targetPlan.name()
                                + "_"
                                + UUID.randomUUID()
                )
                .build();

        stripeSubscription.update(params, options);

        /*
         * No modificamos localSubscription.plan aquí.
         * customer.subscription.updated confirmará el cambio.
         */

        localSubscription.setPendingPlan(null);
        localSubscription.setPendingPlanEffectiveAt(null);

        subscriptionRepository.save(localSubscription);
    }
    
    private void scheduleDowngrade(
            Store store,
            Subscription localSubscription,
            StorePlan targetPlan
    ) throws StripeException {

        String stripeSubscriptionId =
                localSubscription.getStripeSubscriptionId();

        String targetPriceId =
                stripePlanMapper.getPriceId(targetPlan);

        com.stripe.model.Subscription stripeSubscription =
                com.stripe.model.Subscription.retrieve(
                        stripeSubscriptionId
                );

        SubscriptionItem currentItem =
                getSingleSubscriptionItem(stripeSubscription);

        if (currentItem.getPrice() == null) {
            throw new IllegalStateException(
                    "El item actual no tiene precio Stripe"
            );
        }

        String currentPriceId =
                currentItem.getPrice().getId();

        Long currentPeriodStart =
                stripeSubscription.getCurrentPeriodStart();

        Long currentPeriodEnd =
                stripeSubscription.getCurrentPeriodEnd();

        if (currentPeriodStart == null
                || currentPeriodEnd == null) {
            throw new IllegalStateException(
                    "Stripe no devolvió el periodo actual"
            );
        }

        com.stripe.model.SubscriptionSchedule schedule;

        if (stripeSubscription.getSchedule() != null
                && !stripeSubscription.getSchedule().isBlank()) {

            schedule =
                    com.stripe.model.SubscriptionSchedule.retrieve(
                            stripeSubscription.getSchedule()
                    );

        } else {

            var createParams =
                    com.stripe.param.SubscriptionScheduleCreateParams
                            .builder()
                            .setFromSubscription(stripeSubscriptionId)
                            .build();

            schedule =
                    com.stripe.model.SubscriptionSchedule.create(
                            createParams
                    );
        }

        var currentPhase =
                com.stripe.param.SubscriptionScheduleUpdateParams
                        .Phase
                        .builder()
                        .setStartDate(currentPeriodStart)
                        .setEndDate(currentPeriodEnd)
                        .addItem(
                                com.stripe.param
                                        .SubscriptionScheduleUpdateParams
                                        .Phase
                                        .Item
                                        .builder()
                                        .setPrice(currentPriceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

        var downgradePhase =
                com.stripe.param.SubscriptionScheduleUpdateParams
                        .Phase
                        .builder()
                        .setStartDate(currentPeriodEnd)
                        .addItem(
                                com.stripe.param
                                        .SubscriptionScheduleUpdateParams
                                        .Phase
                                        .Item
                                        .builder()
                                        .setPrice(targetPriceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

        var updateParams =
                com.stripe.param.SubscriptionScheduleUpdateParams
                        .builder()
                        .setEndBehavior(
                                com.stripe.param
                                        .SubscriptionScheduleUpdateParams
                                        .EndBehavior
                                        .RELEASE
                        )
                        .addPhase(currentPhase)
                        .addPhase(downgradePhase)
                        .putMetadata(
                                "store_id",
                                store.getId().toString()
                        )
                        .putMetadata(
                                "pending_plan",
                                targetPlan.name()
                        )
                        .putMetadata(
                                "stripe_price_id",
                                targetPriceId
                        )
                        .build();

        schedule.update(updateParams);

        /*
         * El plan actual no cambia.
         * Solo registramos el downgrade pendiente.
         */
        localSubscription.setPendingPlan(targetPlan);
        localSubscription.setPendingPlanEffectiveAt(
                fromStripeEpoch(currentPeriodEnd)
        );

        subscriptionRepository.save(localSubscription);
    }
    private SubscriptionItem getSingleSubscriptionItem(
            com.stripe.model.Subscription stripeSubscription
    ) {

        if (stripeSubscription.getItems() == null
                || stripeSubscription.getItems().getData() == null
                || stripeSubscription.getItems().getData().size() != 1) {
            throw new IllegalStateException(
                    "La suscripción Stripe debe contener exactamente un item"
            );
        }

        return stripeSubscription
                .getItems()
                .getData()
                .get(0);
    }

    private LocalDateTime fromStripeEpoch(Long epochSeconds) {

        if (epochSeconds == null) {
            return null;
        }

        return Instant
                .ofEpochSecond(epochSeconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
}
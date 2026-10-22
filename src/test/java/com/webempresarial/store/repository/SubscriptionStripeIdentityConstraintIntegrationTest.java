package com.webempresarial.store.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class SubscriptionStripeIdentityConstraintIntegrationTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void shouldRejectSameStripeSubscriptionIdAcrossDifferentStores() {

        String suffix =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        Store firstStore =
                createStore(
                        "Constraint A",
                        "constraint-a-"
                                + suffix
                                + ".web-empresarial.com"
                );

        Store secondStore =
                createStore(
                        "Constraint B",
                        "constraint-b-"
                                + suffix
                                + ".web-empresarial.com"
                );

        Subscription first =
                createSubscription(
                        firstStore,
                        "sub_unique_constraint_test"
                );

        subscriptionRepository
                .saveAndFlush(first);

        Subscription duplicate =
                createSubscription(
                        secondStore,
                        "sub_unique_constraint_test"
                );

        assertThatThrownBy(() ->
                subscriptionRepository
                        .saveAndFlush(duplicate)
        )
        .isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    private Store createStore(
            String name,
            String domain
    ) {

        Store store =
                new Store();

        store.setNombre(name);
        store.setDominio(domain);
        store.setActiva(true);
        store.setPlan(StorePlan.BASIC);
        store.setTheme("default");
        store.setCurrency("MXN");

        return storeRepository
                .saveAndFlush(store);
    }

    private Subscription createSubscription(
            Store store,
            String stripeSubscriptionId
    ) {

        Subscription subscription =
                new Subscription();

        subscription.setStore(store);
        subscription.setPlan(StorePlan.BASIC);
        subscription.setStatus(
                SubscriptionStatus.ACTIVE
        );

        subscription.setStripeSubscriptionId(
                stripeSubscriptionId
        );

        subscription.setCurrency("MXN");

        return subscription;
    }
}

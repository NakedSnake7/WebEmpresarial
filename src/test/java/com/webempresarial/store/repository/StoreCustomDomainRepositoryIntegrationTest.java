package com.webempresarial.store.repository;

import com.webempresarial.store.model.Store;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StoreCustomDomainRepositoryIntegrationTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void verifiedActiveCustomDomainMustResolve() {

        String suffix =
                Long.toUnsignedString(
                        System.nanoTime()
                );

        String customDomain =
                "verified-" + suffix + ".example";

        Store store =
                createStore(
                        "Verified Domain Store",
                        "verified-" + suffix
                                + ".web-empresarial.com"
                );

        store.setCustomDomain(customDomain);
        store.setCustomDomainVerified(true);

        storeRepository.saveAndFlush(store);

        Long storeId = store.getId();

        entityManager.clear();

        Optional<Store> result =
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                customDomain
                        );

        assertThat(result)
                .isPresent();

        assertThat(
                result.orElseThrow().getId()
        ).isEqualTo(storeId);

        assertThat(
                storeRepository.existsByCustomDomain(
                        customDomain
                )
        ).isTrue();
    }

    @Test
    void unverifiedCustomDomainMustNotResolve() {

        String suffix =
                Long.toUnsignedString(
                        System.nanoTime()
                );

        String customDomain =
                "pending-" + suffix + ".example";

        Store store =
                createStore(
                        "Pending Domain Store",
                        "pending-" + suffix
                                + ".web-empresarial.com"
                );

        store.setCustomDomain(customDomain);
        store.setCustomDomainVerified(false);

        storeRepository.saveAndFlush(store);

        entityManager.clear();

        Optional<Store> result =
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                customDomain
                        );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void inactiveStoreCustomDomainMustNotResolve() {

        String suffix =
                Long.toUnsignedString(
                        System.nanoTime()
                );

        String customDomain =
                "inactive-" + suffix + ".example";

        Store store =
                createStore(
                        "Inactive Domain Store",
                        "inactive-" + suffix
                                + ".web-empresarial.com"
                );

        store.setCustomDomain(customDomain);
        store.setCustomDomainVerified(true);
        store.setActiva(false);

        storeRepository.saveAndFlush(store);

        entityManager.clear();

        Optional<Store> result =
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                customDomain
                        );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void duplicateCustomDomainMustViolateDatabaseConstraint() {

        String suffix =
                Long.toUnsignedString(
                        System.nanoTime()
                );

        String customDomain =
                "duplicate-" + suffix + ".example";

        Store storeA =
                createStore(
                        "Duplicate Domain A",
                        "duplicate-a-" + suffix
                                + ".web-empresarial.com"
                );

        Store storeB =
                createStore(
                        "Duplicate Domain B",
                        "duplicate-b-" + suffix
                                + ".web-empresarial.com"
                );

        storeA.setCustomDomain(customDomain);
        storeA.setCustomDomainVerified(true);

        storeB.setCustomDomain(customDomain);
        storeB.setCustomDomainVerified(true);

        storeRepository.saveAndFlush(storeA);

        assertThatThrownBy(
                () ->
                        storeRepository.saveAndFlush(
                                storeB
                        )
        )
        .isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    private Store createStore(
            String name,
            String domain
    ) {

        Store store = new Store();

        store.setNombre(name);
        store.setDominio(domain);
        store.setActiva(true);

        return store;
    }
}

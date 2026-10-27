package com.webempresarial.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SubscriptionRepositoryLockingFetchContractTest {

    private static final Path REPOSITORY =
            Path.of(
                    "src/main/java/"
                    + "com/webempresarial/store/repository/"
                    + "SubscriptionRepository.java"
            );

    @Test
    void stripeLockingLookupsMustFetchStoreInSameQuery()
            throws Exception {

        String source =
                Files.readString(REPOSITORY);

        String normalized =
                source.replaceAll(
                        "\\s+",
                        " "
                );

        assertThat(normalized)
                .contains(
                        "SELECT s "
                        + "FROM Subscription s "
                        + "JOIN FETCH s.store "
                        + "WHERE s.stripeSubscriptionId "
                        + "= :stripeSubscriptionId"
                );

        assertThat(normalized)
                .contains(
                        "SELECT s "
                        + "FROM Subscription s "
                        + "JOIN FETCH s.store "
                        + "WHERE s.stripeCustomerId "
                        + "= :stripeCustomerId"
                );
    }
}

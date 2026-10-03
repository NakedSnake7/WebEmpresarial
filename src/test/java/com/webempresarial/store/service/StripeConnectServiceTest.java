package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

class StripeConnectServiceTest {

    private StoreRepository storeRepository;
    private StripeConnectService service;

    @BeforeEach
    void setUp() {

        storeRepository =
                mock(StoreRepository.class);

        service =
                new StripeConnectService(
                        storeRepository
                );
    }

    @Test
    void createOnboardingLink_shouldCreateAccountWhenStoreHasNone()
            throws Exception {

        Store store = mock(Store.class);

        when(store.getId())
                .thenReturn(3L);

        when(store.getStripeConnectedAccountId())
                .thenReturn(null);

        when(store.getCompanyEmail())
                .thenReturn("merchant@test.com");

        Account account = mock(Account.class);

        when(account.getId())
                .thenReturn("acct_new");

        AccountLink accountLink =
                mock(AccountLink.class);

        when(accountLink.getUrl())
                .thenReturn(
                        "https://connect.stripe.com/setup/test"
                );

        AtomicReference<AccountCreateParams>
                accountParamsCaptured =
                new AtomicReference<>();

        AtomicReference<AccountLinkCreateParams>
                linkParamsCaptured =
                new AtomicReference<>();

        try (
                MockedStatic<Account> accountMock =
                        mockStatic(Account.class);

                MockedStatic<AccountLink> linkMock =
                        mockStatic(AccountLink.class)
        ) {

            accountMock.when(() ->
                    Account.create(
                            any(AccountCreateParams.class)
                    )
            ).thenAnswer(invocation -> {

                accountParamsCaptured.set(
                        invocation.getArgument(0)
                );

                return account;
            });

            linkMock.when(() ->
                    AccountLink.create(
                            any(AccountLinkCreateParams.class)
                    )
            ).thenAnswer(invocation -> {

                linkParamsCaptured.set(
                        invocation.getArgument(0)
                );

                return accountLink;
            });

            String result =
                    service.createOnboardingLink(
                            store,
                            "https://stride.test"
                    );

            assertThat(result)
                    .isEqualTo(
                            "https://connect.stripe.com/setup/test"
                    );
        }

        verify(store)
                .setStripeConnectedAccountId(
                        "acct_new"
                );

        verify(store)
                .setStripeConnected(false);

        verify(storeRepository)
                .save(store);

        assertThat(accountParamsCaptured.get())
                .isNotNull();

        assertThat(
                accountParamsCaptured.get()
                        .getType()
        ).isEqualTo(
                AccountCreateParams.Type.STANDARD
        );

        assertThat(
                accountParamsCaptured.get()
                        .getEmail()
        ).isEqualTo(
                "merchant@test.com"
        );

        AccountLinkCreateParams linkParams =
                linkParamsCaptured.get();

        assertThat(linkParams)
                .isNotNull();

        assertThat(linkParams.getAccount())
                .isEqualTo("acct_new");

        assertThat(linkParams.getRefreshUrl())
                .isEqualTo(
                        "https://stride.test"
                                + "/admin/stripe/connect/refresh"
                );

        assertThat(linkParams.getReturnUrl())
                .isEqualTo(
                        "https://stride.test"
                                + "/admin/stripe/connect/return"
                );

        assertThat(linkParams.getType())
                .isEqualTo(
                        AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING
                );
    }

    @Test
    void createOnboardingLink_shouldReuseExistingAccount()
            throws Exception {

        Store store = mock(Store.class);

        when(store.getId())
                .thenReturn(3L);

        when(store.getStripeConnectedAccountId())
                .thenReturn("acct_existing");

        AccountLink accountLink =
                mock(AccountLink.class);

        when(accountLink.getUrl())
                .thenReturn(
                        "https://connect.stripe.com/existing"
                );

        AtomicReference<AccountLinkCreateParams>
                captured =
                new AtomicReference<>();

        try (
                MockedStatic<Account> accountMock =
                        mockStatic(Account.class);

                MockedStatic<AccountLink> linkMock =
                        mockStatic(AccountLink.class)
        ) {

            linkMock.when(() ->
                    AccountLink.create(
                            any(AccountLinkCreateParams.class)
                    )
            ).thenAnswer(invocation -> {

                captured.set(
                        invocation.getArgument(0)
                );

                return accountLink;
            });

            String result =
                    service.createOnboardingLink(
                            store,
                            "https://stride.test"
                    );

            assertThat(result)
                    .isEqualTo(
                            "https://connect.stripe.com/existing"
                    );

            accountMock.verifyNoInteractions();
        }

        assertThat(captured.get().getAccount())
                .isEqualTo("acct_existing");

        verify(store, never())
                .setStripeConnectedAccountId(any());

        verify(store, never())
                .setStripeConnected(false);

        verify(storeRepository, never())
                .save(store);
    }

    @Test
    void syncConnectionStatus_shouldReturnFalseWhenAccountIdIsMissing() {

        Store store = mock(Store.class);

        when(store.getStripeConnectedAccountId())
                .thenReturn(null);

        boolean result =
                service.syncConnectionStatus(store);

        assertThat(result)
                .isFalse();

        verify(store)
                .setStripeConnected(false);

        verify(store)
                .setStripeConnectedAt(null);

        verify(storeRepository)
                .save(store);
    }

    @Test
    void syncConnectionStatus_shouldReturnFalseWhenChargesAreDisabled()
            throws Exception {

        Store store = mock(Store.class);

        when(store.getStripeConnectedAccountId())
                .thenReturn("acct_test");

        Account account = mock(Account.class);

        when(account.getChargesEnabled())
                .thenReturn(false);

        try (MockedStatic<Account> mocked =
                mockStatic(Account.class)) {

            mocked.when(() ->
                    Account.retrieve("acct_test")
            ).thenReturn(account);

            boolean result =
                    service.syncConnectionStatus(store);

            assertThat(result)
                    .isFalse();
        }

        verify(store)
                .setStripeConnected(false);

        verify(store)
                .setStripeConnectedAt(null);

        verify(storeRepository)
                .save(store);
    }

    @Test
    void syncConnectionStatus_shouldMarkStoreConnectedWhenChargesAreEnabled()
            throws Exception {

        Store store = mock(Store.class);

        when(store.getStripeConnectedAccountId())
                .thenReturn("acct_test");

        when(store.getStripeConnectedAt())
                .thenReturn(null);

        Account account = mock(Account.class);

        when(account.getChargesEnabled())
                .thenReturn(true);

        try (MockedStatic<Account> mocked =
                mockStatic(Account.class)) {

            mocked.when(() ->
                    Account.retrieve("acct_test")
            ).thenReturn(account);

            boolean result =
                    service.syncConnectionStatus(store);

            assertThat(result)
                    .isTrue();
        }

        verify(store)
                .setStripeConnected(true);

        verify(store)
                .setStripeConnectedAt(
                        any(LocalDateTime.class)
                );

        verify(storeRepository)
                .save(store);
    }

    @Test
    void syncConnectionStatus_shouldPreserveExistingConnectedAt()
            throws Exception {

        Store store = mock(Store.class);

        LocalDateTime existing =
                LocalDateTime.of(
                        2026,
                        9,
                        8,
                        12,
                        0
                );

        when(store.getStripeConnectedAccountId())
                .thenReturn("acct_test");

        when(store.getStripeConnectedAt())
                .thenReturn(existing);

        Account account = mock(Account.class);

        when(account.getChargesEnabled())
                .thenReturn(true);

        try (MockedStatic<Account> mocked =
                mockStatic(Account.class)) {

            mocked.when(() ->
                    Account.retrieve("acct_test")
            ).thenReturn(account);

            boolean result =
                    service.syncConnectionStatus(store);

            assertThat(result)
                    .isTrue();
        }

        verify(store)
                .setStripeConnected(true);

        verify(store, never())
                .setStripeConnectedAt(
                        any(LocalDateTime.class)
                );

        verify(storeRepository)
                .save(store);
    }

    @Test
    void syncConnectionStatus_shouldFailClosedWhenStripeLookupFails()
            throws Exception {

        Store store = mock(Store.class);

        when(store.getStripeConnectedAccountId())
                .thenReturn("acct_error");

        try (MockedStatic<Account> mocked =
                mockStatic(Account.class)) {

            mocked.when(() ->
                    Account.retrieve("acct_error")
            ).thenThrow(
                    new RuntimeException(
                            "Stripe unavailable"
                    )
            );

            assertThatThrownBy(() ->
                    service.syncConnectionStatus(store)
            )
                    .isInstanceOf(
                            RuntimeException.class
                    )
                    .hasMessage(
                            "No se pudo verificar el estado de Stripe Connect"
                    );
        }

        verify(store)
                .setStripeConnected(false);

        verify(store)
                .setStripeConnectedAt(null);

        verify(storeRepository)
                .save(store);
    }
}

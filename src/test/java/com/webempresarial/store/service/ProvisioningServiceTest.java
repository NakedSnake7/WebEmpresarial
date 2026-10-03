package com.webempresarial.store.service;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.event.AdminAccountCreatedEvent;

import org.springframework.context.ApplicationEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProvisioningServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminAccountActivationService
            adminAccountActivationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StoreSettingsService storeSettingsService;

    private ProvisioningService provisioningService;

    @BeforeEach
    void setUp() {

        provisioningService =
                new ProvisioningService(
                        storeRepository,
                        subscriptionRepository,
                        adminUserRepository,
                        passwordEncoder,
                        adminAccountActivationService,
                        storeSettingsService,
                        eventPublisher
                );
    }

    @Test
    void shouldProvisionStoreSubscriptionAndStoreAdminFromCheckout() {

        when(
                storeRepository.findByDominio(
                        "acme.web-empresarial.com"
                )
        ).thenReturn(Optional.empty());

        when(
                storeRepository.save(any(Store.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );


        when(
                adminUserRepository.existsByEmail(
                        "owner@acme.test"
                )
        ).thenReturn(false);

        when(
                adminUserRepository.save(
                        any(AdminUser.class)
                )
        ).thenAnswer(invocation -> {

            AdminUser admin =
                    invocation.getArgument(0);

            admin.setId(101L);

            return admin;
        });


        when(
                passwordEncoder.encode(any(String.class))
        ).thenReturn("encoded-temporary-password");

        AdminAccountActivationToken activationToken =
                new AdminAccountActivationToken();

        activationToken.setToken(
                "activation-token-123"
        );

        when(
                adminAccountActivationService
                        .createToken(any(AdminUser.class))
        ).thenReturn(activationToken);

        Store result =
                provisioningService
                        .provisionStoreFromCheckout(
                                "ACME",
                                "acme",
                                "Alice Owner",
                                " Owner@ACME.TEST ",
                                StorePlan.PREMIUM,
                                "cus_acme",
                                "sub_acme",
                                "price_premium"
                        );

        /*
         * =====================================================
         * STORE
         * =====================================================
         */

        ArgumentCaptor<Store> storeCaptor =
                ArgumentCaptor.forClass(Store.class);

        verify(storeRepository)
                .save(storeCaptor.capture());

        Store savedStore =
                storeCaptor.getValue();

        verify(storeSettingsService)
                .createDefaults(savedStore);

        assertThat(result)
                .isSameAs(savedStore);

        assertThat(savedStore.getNombre())
                .isEqualTo("ACME");

        assertThat(savedStore.getDominio())
                .isEqualTo(
                        "acme.web-empresarial.com"
                );

        assertThat(savedStore.getPlan())
                .isEqualTo(StorePlan.PREMIUM);

        assertThat(savedStore.isActiva())
                .isTrue();

        assertThat(savedStore.getTheme())
                .isEqualTo("default");

        assertThat(savedStore.getContactName())
                .isEqualTo("Alice Owner");

        assertThat(savedStore.getCompanyEmail())
                .isEqualTo("owner@acme.test");

        assertThat(savedStore.getCurrency())
                .isEqualTo("MXN");

        /*
         * =====================================================
         * SUBSCRIPTION
         * =====================================================
         */

        ArgumentCaptor<Subscription> subscriptionCaptor =
                ArgumentCaptor.forClass(
                        Subscription.class
                );

        verify(subscriptionRepository)
                .save(subscriptionCaptor.capture());

        Subscription subscription =
                subscriptionCaptor.getValue();

        assertThat(subscription.getStore())
                .isSameAs(savedStore);

        assertThat(subscription.getPlan())
                .isEqualTo(StorePlan.PREMIUM);

        assertThat(subscription.getStatus())
                .isEqualTo(
                        SubscriptionStatus.ACTIVE
                );

        assertThat(
                subscription.getStripeCustomerId()
        ).isEqualTo("cus_acme");

        assertThat(
                subscription.getStripeSubscriptionId()
        ).isEqualTo("sub_acme");

        assertThat(
                subscription.getStripePriceId()
        ).isEqualTo("price_premium");

        assertThat(subscription.getStartsAt())
                .isNotNull();

        assertThat(
                subscription.getCurrentPeriodStart()
        ).isNotNull();

        assertThat(
                subscription.getCurrentPeriodEnd()
        ).isNotNull();

        assertThat(
                subscription.getNextBillingDate()
        ).isNotNull();

        /*
         * La implementación actual crea el periodo
         * inicial con aproximadamente un mes de duración.
         */
        assertThat(
                subscription.getCurrentPeriodEnd()
        ).isAfter(
                subscription.getCurrentPeriodStart()
        );

        /*
         * =====================================================
         * STORE ADMIN
         * =====================================================
         */

        ArgumentCaptor<AdminUser> adminCaptor =
                ArgumentCaptor.forClass(
                        AdminUser.class
                );

        verify(adminUserRepository)
                .save(adminCaptor.capture());

        AdminUser admin =
                adminCaptor.getValue();

        assertThat(admin.getFullName())
                .isEqualTo("Alice Owner");

        assertThat(admin.getEmail())
                .isEqualTo("owner@acme.test");

        assertThat(admin.getRole())
                .isEqualTo(
                        AdminRole.STORE_ADMIN
                );

        assertThat(admin.isEnabled())
        .isFalse();

        assertThat(admin.getStore())
                .isSameAs(savedStore);

        assertThat(admin.getPassword())
                .isEqualTo(
                        "encoded-temporary-password"
                );
        verify(adminAccountActivationService)
        .createToken(admin);

ArgumentCaptor<AdminAccountCreatedEvent> eventCaptor =
        ArgumentCaptor.forClass(
                AdminAccountCreatedEvent.class
        );

verify(eventPublisher)
        .publishEvent(
                eventCaptor.capture()
        );

AdminAccountCreatedEvent event =
        eventCaptor.getValue();

assertThat(event.adminUserId())
        .isEqualTo(101L);

assertThat(event.email())
        .isEqualTo("owner@acme.test");

assertThat(event.fullName())
        .isEqualTo("Alice Owner");

assertThat(event.storeName())
        .isEqualTo("ACME");

assertThat(event.storeDomain())
.isEqualTo(
        "acme.web-empresarial.com"
);

assertThat(event.activationToken())
        .isEqualTo("activation-token-123");
        /*
         * También comprobamos que la contraseña
         * temporal realmente fue generada.
         */
        ArgumentCaptor<String> passwordCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(passwordEncoder)
                .encode(
                        passwordCaptor.capture()
                );

        String temporaryPassword =
                passwordCaptor.getValue();

        assertThat(temporaryPassword)
                .isNotBlank();

        assertThat(temporaryPassword)
                .hasSize(12);
    }

    @Test
    void shouldNormalizeDomainBeforeProvisioning() {

        when(
                storeRepository.findByDominio(
                        "stride.web-empresarial.com"
                )
        ).thenReturn(Optional.empty());

        when(
                storeRepository.save(any(Store.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                adminUserRepository.existsByEmail(
                        "owner@stride.test"
                )
        ).thenReturn(false);

        when(
                adminUserRepository.save(
                        any(AdminUser.class)
                )
        ).thenAnswer(invocation -> {

            AdminUser admin =
                    invocation.getArgument(0);

            admin.setId(102L);

            return admin;
        });

        when(
                passwordEncoder.encode(any(String.class))
        ).thenReturn("encoded");

        AdminAccountActivationToken activationToken =
                new AdminAccountActivationToken();

        activationToken.setToken(
                "stride-activation-token"
        );

        when(
                adminAccountActivationService
                        .createToken(any(AdminUser.class))
        ).thenReturn(activationToken);

        Store store =
                provisioningService
                        .provisionStoreFromCheckout(
                                "Stride",
                                " HTTPS://STRIDE.WEB-EMPRESARIAL.COM/ ",
                                "Stride Owner",
                                "owner@stride.test",
                                StorePlan.PRO,
                                "cus_stride",
                                "sub_stride",
                                "price_pro"
                        );

        assertThat(store.getDominio())
                .isEqualTo(
                        "stride.web-empresarial.com"
                );

        verify(storeSettingsService)
                .createDefaults(store);
    }

    @Test
    void shouldReturnExistingStoreWithoutCreatingDuplicateResources() {

        Store existingStore =
                new Store();

        existingStore.setId(3L);

        existingStore.setNombre("Existing");

        existingStore.setDominio(
                "existing.web-empresarial.com"
        );

        when(
                storeRepository.findByDominio(
                        "existing.web-empresarial.com"
                )
        ).thenReturn(
                Optional.of(existingStore)
        );

        Store result =
                provisioningService
                        .provisionStoreFromCheckout(
                                "Existing",
                                "existing",
                                "Owner",
                                "owner@existing.test",
                                StorePlan.PREMIUM,
                                "cus_existing",
                                "sub_existing",
                                "price_premium"
                        );

        assertThat(result)
                .isSameAs(existingStore);

        verify(storeRepository, never())
                .save(any(Store.class));

        verifyNoInteractions(
                subscriptionRepository
        );

        verifyNoInteractions(
                adminUserRepository
        );

        verifyNoInteractions(
                passwordEncoder
        );
        verifyNoInteractions(
                adminAccountActivationService
        );
        verifyNoInteractions(storeSettingsService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectProvisioningWhenAdminEmailAlreadyExists() {

        when(
                storeRepository.findByDominio(
                        "acme.web-empresarial.com"
                )
        ).thenReturn(Optional.empty());

        when(
                adminUserRepository.existsByEmail(
                        "existing@acme.test"
                )
        ).thenReturn(true);

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() ->
                        provisioningService
                                .provisionStoreFromCheckout(
                                        "ACME",
                                        "acme",
                                        "Existing Owner",
                                        "existing@acme.test",
                                        StorePlan.PRO,
                                        "cus_acme",
                                        "sub_acme",
                                        "price_pro"
                                )
                )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Ya existe una cuenta administrativa con este correo"
                );

        verify(storeRepository, never())
                .save(any(Store.class));

        verifyNoInteractions(subscriptionRepository);

        verify(adminUserRepository, never())
                .save(any(AdminUser.class));

        verifyNoInteractions(storeSettingsService);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(adminAccountActivationService);
        verifyNoInteractions(eventPublisher);
    }
}

package com.webempresarial.store.service;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.event.AdminAccountCreatedEvent;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProvisioningExistingStoreOwnerTest {

    private StoreRepository storeRepository;
    private SubscriptionRepository subscriptionRepository;
    private AdminUserRepository adminUserRepository;
    private PasswordEncoder passwordEncoder;
    private AdminAccountActivationService activationService;
    private StoreSettingsService storeSettingsService;
    private ApplicationEventPublisher eventPublisher;

    private ProvisioningService service;

    @BeforeEach
    void setUp() {

        storeRepository =
                mock(StoreRepository.class);

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        adminUserRepository =
                mock(AdminUserRepository.class);

        passwordEncoder =
                mock(PasswordEncoder.class);

        activationService =
                mock(AdminAccountActivationService.class);

        storeSettingsService =
                mock(StoreSettingsService.class);

        eventPublisher =
                mock(ApplicationEventPublisher.class);

        service =
                new ProvisioningService(
                        storeRepository,
                        subscriptionRepository,
                        adminUserRepository,
                        passwordEncoder,
                        activationService,
                        storeSettingsService,
                        eventPublisher
                );
    }

    @Test
    void missingStoreMustFailClosed() {

        when(
                storeRepository.findById(999L)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () ->
                        service
                                .provisionOwnerForExistingStore(
                                        999L,
                                        "Owner",
                                        "owner@test.com"
                                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("La tienda no existe");

        verifyNoInteractions(
                subscriptionRepository,
                adminUserRepository,
                passwordEncoder,
                activationService,
                eventPublisher
        );
    }

    @Test
    void inactiveStoreMustFailClosed() {

        Store store =
                store(
                        4L,
                        "STRIDE",
                        "stride.web-empresarial.com",
                        false
                );

        when(
                storeRepository.findById(4L)
        ).thenReturn(Optional.of(store));

        assertThatThrownBy(
                () ->
                        service
                                .provisionOwnerForExistingStore(
                                        4L,
                                        "Owner",
                                        "owner@test.com"
                                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("La tienda está desactivada");

        verifyNoInteractions(
                subscriptionRepository,
                adminUserRepository,
                passwordEncoder,
                activationService,
                eventPublisher
        );
    }

    @Test
    void storeWithoutSubscriptionMustFailClosed() {

        Store store =
                store(
                        4L,
                        "STRIDE",
                        "stride.web-empresarial.com",
                        true
                );

        when(
                storeRepository.findById(4L)
        ).thenReturn(Optional.of(store));

        when(
                subscriptionRepository.findByStoreId(4L)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () ->
                        service
                                .provisionOwnerForExistingStore(
                                        4L,
                                        "Owner",
                                        "owner@test.com"
                                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
                "La tienda no tiene una suscripción SaaS asociada"
        );

        verifyNoInteractions(
                adminUserRepository,
                passwordEncoder,
                activationService,
                eventPublisher
        );
    }

    @Test
    void storeWithExistingOwnerMustFailClosed() {

        Store store =
                activeTenant();

        when(
                storeRepository.findById(4L)
        ).thenReturn(Optional.of(store));

        when(
                subscriptionRepository.findByStoreId(4L)
        ).thenReturn(
                Optional.of(
                        mock(Subscription.class)
                )
        );

        when(
                adminUserRepository
                        .existsByStoreIdAndRole(
                                4L,
                                AdminRole.STORE_OWNER
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () ->
                        service
                                .provisionOwnerForExistingStore(
                                        4L,
                                        "Owner",
                                        "owner@test.com"
                                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
                "La tienda ya tiene un propietario"
        );

        verify(
                adminUserRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                passwordEncoder,
                activationService,
                eventPublisher
        );
    }

    @Test
    void duplicatedEmailMustFailClosed() {

        Store store =
                activeTenant();

        when(
                storeRepository.findById(4L)
        ).thenReturn(Optional.of(store));

        when(
                subscriptionRepository.findByStoreId(4L)
        ).thenReturn(
                Optional.of(
                        mock(Subscription.class)
                )
        );

        when(
                adminUserRepository
                        .existsByStoreIdAndRole(
                                4L,
                                AdminRole.STORE_OWNER
                        )
        ).thenReturn(false);

        when(
                adminUserRepository.existsByEmail(
                        "owner@test.com"
                )
        ).thenReturn(true);

        assertThatThrownBy(
                () ->
                        service
                                .provisionOwnerForExistingStore(
                                        4L,
                                        "Owner",
                                        " OWNER@Test.COM "
                                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
                "El correo ya está asociado "
                + "a una cuenta administrativa"
        );

        verify(
                adminUserRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                passwordEncoder,
                activationService,
                eventPublisher
        );
    }

    @Test
    void validExistingTenantMustCreateDisabledOwnerAndInvitation() {

        Store store =
                activeTenant();

        when(
                storeRepository.findById(4L)
        ).thenReturn(Optional.of(store));

        when(
                subscriptionRepository.findByStoreId(4L)
        ).thenReturn(
                Optional.of(
                        mock(Subscription.class)
                )
        );

        when(
                adminUserRepository
                        .existsByStoreIdAndRole(
                                4L,
                                AdminRole.STORE_OWNER
                        )
        ).thenReturn(false);

        when(
                adminUserRepository.existsByEmail(
                        "owner@stride.test"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(anyString())
        ).thenReturn("encoded-temporary-password");

        when(
                adminUserRepository.save(
                        any(AdminUser.class)
                )
        ).thenAnswer(invocation -> {

            AdminUser admin =
                    invocation.getArgument(0);

            admin.setId(42L);

            return admin;
        });

        AdminAccountActivationToken activationToken =
                mock(
                        AdminAccountActivationToken.class
                );

        when(
                activationToken.getToken()
        ).thenReturn("activation-token");

        when(
                activationService.createToken(
                        any(AdminUser.class)
                )
        ).thenReturn(activationToken);

        AdminUser result =
                service.provisionOwnerForExistingStore(
                        4L,
                        "  Stride Owner  ",
                        " OWNER@STRIDE.TEST "
                );

        assertThat(result.getId())
                .isEqualTo(42L);

        assertThat(result.getFullName())
                .isEqualTo("Stride Owner");

        assertThat(result.getEmail())
                .isEqualTo(
                        "owner@stride.test"
                );

        assertThat(result.getRole())
                .isEqualTo(
                        AdminRole.STORE_OWNER
                );

        assertThat(result.isEnabled())
                .isFalse();

        assertThat(result.getStore())
                .isSameAs(store);

        assertThat(result.getPassword())
                .isEqualTo(
                        "encoded-temporary-password"
                );

        verify(passwordEncoder)
                .encode(anyString());

        verify(activationService)
                .createToken(result);

        ArgumentCaptor<AdminAccountCreatedEvent>
                eventCaptor =
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
                .isEqualTo(42L);

        assertThat(event.email())
                .isEqualTo(
                        "owner@stride.test"
                );

        assertThat(event.fullName())
                .isEqualTo(
                        "Stride Owner"
                );

        assertThat(event.storeName())
                .isEqualTo("STRIDE");

        assertThat(event.storeDomain())
                .isEqualTo(
                        "stride.web-empresarial.com"
                );

        assertThat(event.activationToken())
                .isEqualTo(
                        "activation-token"
                );
    }

    private Store activeTenant() {
        return store(
                4L,
                "STRIDE",
                "stride.web-empresarial.com",
                true
        );
    }

    private Store store(
            Long id,
            String name,
            String domain,
            boolean active
    ) {

        Store store = new Store();

        store.setId(id);
        store.setNombre(name);
        store.setDominio(domain);
        store.setActiva(active);

        return store;
    }
}

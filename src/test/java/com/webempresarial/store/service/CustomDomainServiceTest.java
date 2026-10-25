package com.webempresarial.store.service;

import com.webempresarial.store.exceptions.CustomDomainValidationException;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomDomainServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PlatformAccessService platformAccessService;

    @Mock
    private FeatureAccessService featureAccessService;

    private CustomDomainService service;

    private Store store;

    @BeforeEach
    void setUp() {

        service = new CustomDomainService(
                storeRepository,
                platformAccessService,
                featureAccessService
        );

        store = new Store();
        store.setId(10L);
        store.setDominio(
                "stride.web-empresarial.com"
        );
        store.setActiva(true);

        when(
                platformAccessService.hasPlatformAccess(store)
        ).thenReturn(true);
    }

    @Test
    void requestMustNormalizeDomainAndResetVerification() {

        store.setCustomDomain("old-domain.mx");
        store.setCustomDomainVerified(true);

        service.requestDomain(
                store,
                "  Cliente.COM.  "
        );

        assertThat(store.getCustomDomain())
                .isEqualTo("cliente.com");

        assertThat(store.isCustomDomainVerified())
                .isFalse();

        verify(storeRepository)
                .save(store);
    }

    @Test
    void requestMustNormalizeInternationalDomainToAscii() {

        service.requestDomain(
                store,
                "münchen.de"
        );

        assertThat(store.getCustomDomain())
                .isEqualTo(
                        "xn--mnchen-3ya.de"
                );

        assertThat(store.isCustomDomainVerified())
                .isFalse();
    }

    @Test
    void requestingSameDomainMustBeIdempotent() {

        store.setCustomDomain("cliente.com");
        store.setCustomDomainVerified(true);

        service.requestDomain(
                store,
                "CLIENTE.COM."
        );

        assertThat(store.getCustomDomain())
                .isEqualTo("cliente.com");

        assertThat(store.isCustomDomainVerified())
                .isTrue();

        verify(
                storeRepository,
                never()
        ).save(store);
    }

    @Test
    void requestMustRejectMultipleTrailingRootDots() {

        org.assertj.core.api.Assertions
                .assertThatThrownBy(
                        () ->
                                service.requestDomain(
                                        store,
                                        "cliente.com.."
                                )
                )
                .isInstanceOf(
                        com.webempresarial.store.exceptions
                                .CustomDomainValidationException.class
                );

        org.mockito.Mockito.verifyNoInteractions(
                storeRepository
        );
    }

    @Test
    void requestMustRejectUrl() {

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "https://cliente.com"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );
    }

    @Test
    void requestMustRejectHostWithPathOrPort() {

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "cliente.com/path"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "cliente.com:443"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );
    }

    @Test
    void requestMustRejectWildcardLocalhostAndIp() {

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "*.cliente.com"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "localhost"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "127.0.0.1"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );
    }

    @Test
    void requestMustRejectPlatformNamespace() {

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "web-empresarial.com"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "otro.web-empresarial.com"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );
    }

    @Test
    void requestMustRejectPrimaryDomainCollision() {

        when(
                storeRepository.existsByDominio(
                        "cliente.com"
                )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "cliente.com"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        )
        .hasMessageContaining(
                "uso"
        );

        verify(
                storeRepository,
                never()
        ).save(store);
    }

    @Test
    void requestMustRejectCustomDomainCollision() {

        when(
                storeRepository.existsByDominio(
                        "cliente.com"
                )
        ).thenReturn(false);

        when(
                storeRepository.existsByCustomDomain(
                        "cliente.com"
                )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "cliente.com"
                )
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        )
        .hasMessageContaining(
                "uso"
        );
    }

    @Test
    void requestMustRequirePlatformAccess() {

        when(
                platformAccessService.hasPlatformAccess(store)
        ).thenReturn(false);

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "cliente.com"
                )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verify(
                featureAccessService,
                never()
        ).requireFeature(
                store,
                Feature.CUSTOM_DOMAIN
        );
    }

    @Test
    void requestMustRequireCustomDomainEntitlement() {

        doThrow(
                new FeatureLockedException(
                        Feature.CUSTOM_DOMAIN
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.CUSTOM_DOMAIN
        );

        assertThatThrownBy(
                () -> service.requestDomain(
                        store,
                        "cliente.com"
                )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );
    }

    @Test
    void markVerifiedMustRequireConfiguredDomain() {

        assertThatThrownBy(
                () -> service.markVerified(store)
        )
        .isInstanceOf(
                CustomDomainValidationException.class
        );

        verify(
                storeRepository,
                never()
        ).save(store);
    }

    @Test
    void markVerifiedMustActivateConfiguredDomain() {

        store.setCustomDomain("cliente.com");
        store.setCustomDomainVerified(false);

        service.markVerified(store);

        assertThat(store.isCustomDomainVerified())
                .isTrue();

        verify(storeRepository)
                .save(store);
    }

    @Test
    void disconnectMustRemoveAliasWithoutTouchingPlatformDomain() {

        store.setCustomDomain("cliente.com");
        store.setCustomDomainVerified(true);

        service.disconnectDomain(store);

        assertThat(store.getCustomDomain())
                .isNull();

        assertThat(store.isCustomDomainVerified())
                .isFalse();

        assertThat(store.getDominio())
                .isEqualTo(
                        "stride.web-empresarial.com"
                );

        verify(storeRepository)
                .save(store);
    }
}

package com.webempresarial.store.service;

import com.webempresarial.store.exceptions.StoreNotFoundException;
import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreLookupServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PlatformAccessService platformAccessService;

    @Mock
    private FeatureAccessService featureAccessService;

    private StoreLookupService service() {
        return new StoreLookupService(
                storeRepository,
                platformAccessService,
                featureAccessService
        );
    }

    @Test
    void platformDomainMustResolveFirst() {

        Store platformStore = new Store();

        when(
                storeRepository.findByDominioAndActivaTrue(
                        "stride.web-empresarial.com"
                )
        ).thenReturn(Optional.of(platformStore));

        Store result =
                service().getStoreByDomain(
                        "stride.web-empresarial.com"
                );

        assertThat(result)
                .isSameAs(platformStore);

        verify(
                storeRepository,
                never()
        ).findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                "stride.web-empresarial.com"
        );

        verify(
                platformAccessService,
                never()
        ).hasPlatformAccess(platformStore);

        verify(
                featureAccessService,
                never()
        ).canUse(platformStore, Feature.CUSTOM_DOMAIN);
    }

    @Test
    void verifiedCustomDomainMustResolveAsFallback() {

        Store customDomainStore = new Store();

        when(
                storeRepository.findByDominioAndActivaTrue(
                        "stride.mx"
                )
        ).thenReturn(Optional.empty());

        when(
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                "stride.mx"
                        )
        ).thenReturn(Optional.of(customDomainStore));

        when(
                platformAccessService.hasPlatformAccess(
                        customDomainStore
                )
        ).thenReturn(true);

        when(
                featureAccessService.canUse(
                        customDomainStore,
                        Feature.CUSTOM_DOMAIN
                )
        ).thenReturn(true);

        Store result =
                service().getStoreByDomain("stride.mx");

        assertThat(result)
                .isSameAs(customDomainStore);
    }

    @Test
    void customDomainWithoutPlatformAccessMustNotResolve() {

        Store customDomainStore = new Store();

        when(
                storeRepository.findByDominioAndActivaTrue(
                        "suspended.mx"
                )
        ).thenReturn(Optional.empty());

        when(
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                "suspended.mx"
                        )
        ).thenReturn(Optional.of(customDomainStore));

        when(
                platformAccessService.hasPlatformAccess(
                        customDomainStore
                )
        ).thenReturn(false);

        assertThatThrownBy(
                () -> service().getStoreByDomain(
                        "suspended.mx"
                )
        )
        .isInstanceOf(StoreNotFoundException.class)
        .hasMessageContaining("suspended.mx");

        verify(
                featureAccessService,
                never()
        ).canUse(
                customDomainStore,
                Feature.CUSTOM_DOMAIN
        );
    }

    @Test
    void customDomainWithoutFeatureEntitlementMustNotResolve() {

        Store customDomainStore = new Store();

        when(
                storeRepository.findByDominioAndActivaTrue(
                        "basic.mx"
                )
        ).thenReturn(Optional.empty());

        when(
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                "basic.mx"
                        )
        ).thenReturn(Optional.of(customDomainStore));

        when(
                platformAccessService.hasPlatformAccess(
                        customDomainStore
                )
        ).thenReturn(true);

        when(
                featureAccessService.canUse(
                        customDomainStore,
                        Feature.CUSTOM_DOMAIN
                )
        ).thenReturn(false);

        assertThatThrownBy(
                () -> service().getStoreByDomain(
                        "basic.mx"
                )
        )
        .isInstanceOf(StoreNotFoundException.class)
        .hasMessageContaining("basic.mx");
    }

    @Test
    void unverifiedCustomDomainMustNotResolve() {

        when(
                storeRepository.findByDominioAndActivaTrue(
                        "pending.mx"
                )
        ).thenReturn(Optional.empty());

        when(
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                "pending.mx"
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service().getStoreByDomain(
                        "pending.mx"
                )
        )
        .isInstanceOf(StoreNotFoundException.class)
        .hasMessageContaining("pending.mx");
    }

    @Test
    void inactiveCustomDomainMustNotResolve() {

        when(
                storeRepository.findByDominioAndActivaTrue(
                        "inactive.mx"
                )
        ).thenReturn(Optional.empty());

        when(
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                "inactive.mx"
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service().getStoreByDomain(
                        "inactive.mx"
                )
        )
        .isInstanceOf(StoreNotFoundException.class)
        .hasMessageContaining("inactive.mx");
    }

    @Test
    void platformDomainMustWinCrossColumnCollision() {

        Store platformStore = new Store();

        when(
                storeRepository.findByDominioAndActivaTrue(
                        "victim.web-empresarial.com"
                )
        ).thenReturn(Optional.of(platformStore));

        Store result =
                service().getStoreByDomain(
                        "victim.web-empresarial.com"
                );

        assertThat(result)
                .isSameAs(platformStore);

        verify(
                storeRepository,
                never()
        ).findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                "victim.web-empresarial.com"
        );
    }

    @Test
    void unknownDomainMustFailClosed() {

        when(
                storeRepository.findByDominioAndActivaTrue(
                        "unknown.example"
                )
        ).thenReturn(Optional.empty());

        when(
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                "unknown.example"
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service().getStoreByDomain(
                        "unknown.example"
                )
        )
        .isInstanceOf(StoreNotFoundException.class)
        .hasMessageContaining("unknown.example");
    }
}

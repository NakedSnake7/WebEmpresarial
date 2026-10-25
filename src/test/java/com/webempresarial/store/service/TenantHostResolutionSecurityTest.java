package com.webempresarial.store.service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantHostResolutionSecurityTest {

    @Mock
    private StoreLookupService storeLookupService;

    @Mock
    private HttpServletRequest request;

    @Test
    void storeContextMustIgnoreSpoofedForwardedHost() {

        Store expected = new Store();

        when(request.getHeader("Host"))
                .thenReturn(
                        "barleypunch.web-empresarial.com"
                );

        when(
                storeLookupService.getStoreByDomain(
                        "barleypunch.web-empresarial.com"
                )
        ).thenReturn(expected);

        StoreContextService service =
                new StoreContextService(
                        storeLookupService
                );

        Store actual =
                service.getCurrentStore(request);

        assertThat(actual)
                .isSameAs(expected);

        verify(storeLookupService)
                .getStoreByDomain(
                        "barleypunch.web-empresarial.com"
                );

        verify(request, never())
                .getHeader("X-Forwarded-Host");
    }

    @Test
    void storeResolverMustIgnoreSpoofedForwardedHost() {

        Store expected = new Store();

        when(request.getHeader("Host"))
                .thenReturn(
                        "barleypunch.web-empresarial.com"
                );

        when(
                storeLookupService.getStoreByDomain(
                        "barleypunch.web-empresarial.com"
                )
        ).thenReturn(expected);

        StoreResolver resolver =
                new StoreResolver(
                        storeLookupService
                );

        Store actual =
                resolver.getCurrentStore(request);

        assertThat(actual)
                .isSameAs(expected);

        verify(storeLookupService)
                .getStoreByDomain(
                        "barleypunch.web-empresarial.com"
                );

        verify(request, never())
                .getHeader("X-Forwarded-Host");
    }

    @Test
    void hostPortMustBeNormalized() {

        Store expected = new Store();

        when(request.getHeader("Host"))
                .thenReturn(
                        "BarleyPunch.Web-Empresarial.com:443"
                );

        when(
                storeLookupService.getStoreByDomain(
                        "barleypunch.web-empresarial.com"
                )
        ).thenReturn(expected);

        StoreContextService service =
                new StoreContextService(
                        storeLookupService
                );

        assertThat(
                service.getCurrentStore(request)
        ).isSameAs(expected);
    }

    @Test
    void forwardedHostWithoutHostMustFailClosed() {

        when(request.getHeader("Host"))
                .thenReturn(null);

        StoreContextService service =
                new StoreContextService(
                        storeLookupService
                );

        assertThatThrownBy(
                () -> service.getCurrentStore(request)
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
                "dominio"
        );

        verify(request, never())
                .getHeader("X-Forwarded-Host");

        verifyNoInteractions(
                storeLookupService
        );
    }
}

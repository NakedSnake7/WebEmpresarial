package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.CustomDomainService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CustomDomainControllerTest {

    private StoreContextService storeContextService;
    private FeatureAccessService featureAccessService;
    private CustomDomainService customDomainService;
    private CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    private HttpServletRequest request;
    private Store store;

    private CustomDomainController controller;

    @BeforeEach
    void setUp() {

        storeContextService =
                mock(StoreContextService.class);

        featureAccessService =
                mock(FeatureAccessService.class);

        customDomainService =
                mock(CustomDomainService.class);

        currentAdminAuthorizationService =
                mock(CurrentAdminAuthorizationService.class);

        request =
                mock(HttpServletRequest.class);

        store = new Store();
        store.setId(10L);
        store.setDominio(
                "stride.web-empresarial.com"
        );

        when(
                storeContextService.getCurrentStore(request)
        ).thenReturn(store);

        controller =
                new CustomDomainController(
                        storeContextService,
                        featureAccessService,
                        customDomainService,
                        currentAdminAuthorizationService
                );
    }

    @Test
    void getMustRequireFeatureAndExposeStore() {

        when(
                currentAdminAuthorizationService
                        .hasPermission(
                                AdminPermission.DOMAIN_MANAGE
                        )
        ).thenReturn(true);

        ExtendedModelMap model =
                new ExtendedModelMap();

        String view =
                controller.domains(
                        request,
                        model
                );

        assertThat(view)
                .isEqualTo(
                        "admin/domains/index"
                );

        assertThat(
                model.getAttribute("store")
        ).isSameAs(store);

        assertThat(
                model.getAttribute(
                        "canManageDomain"
                )
        ).isEqualTo(true);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CUSTOM_DOMAIN
                );

        verify(
                currentAdminAuthorizationService
        ).hasPermission(
                AdminPermission.DOMAIN_MANAGE
        );

        verifyNoInteractions(
                customDomainService
        );
    }

    @Test
    void getMustExposeReadOnlyStateWithoutManagePermission() {

        when(
                currentAdminAuthorizationService
                        .hasPermission(
                                AdminPermission.DOMAIN_MANAGE
                        )
        ).thenReturn(false);

        ExtendedModelMap model =
                new ExtendedModelMap();

        controller.domains(
                request,
                model
        );

        assertThat(
                model.getAttribute(
                        "canManageDomain"
                )
        ).isEqualTo(false);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CUSTOM_DOMAIN
                );
    }

    @Test
    void postMustRequireFeatureAndRequestDomain() {

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view =
                controller.requestDomain(
                        " Cliente.COM. ",
                        request,
                        redirect
                );

        assertThat(view)
                .isEqualTo(
                        "redirect:/admin/domains"
                );

        assertThat(
                redirect
                        .getFlashAttributes()
                        .get("domainSuccess")
        ).isEqualTo(
                "Dominio guardado. "
                + "La verificación DNS está pendiente."
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CUSTOM_DOMAIN
                );

        verify(customDomainService)
                .requestDomain(
                        store,
                        " Cliente.COM. "
                );
    }

    @Test
    void disconnectMustRequireFeatureAndDisconnectDomain() {

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view =
                controller.disconnectDomain(
                        request,
                        redirect
                );

        assertThat(view)
                .isEqualTo(
                        "redirect:/admin/domains"
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CUSTOM_DOMAIN
                );

        verify(customDomainService)
                .disconnectDomain(store);
    }

    @Test
    void verifyMustRequireFeatureAndMarkDomainVerified() {

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view =
                controller.verifyDomain(
                        request,
                        redirect
                );

        assertThat(view)
                .isEqualTo(
                        "redirect:/admin/domains"
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CUSTOM_DOMAIN
                );

        verify(customDomainService)
                .markVerified(store);
    }
}

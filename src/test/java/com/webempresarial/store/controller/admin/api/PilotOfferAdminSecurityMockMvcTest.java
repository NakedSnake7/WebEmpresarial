package com.webempresarial.store.controller.admin.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.webempresarial.store.config.AdminAuthenticationSuccessHandler;
import com.webempresarial.store.config.FeatureViewAdvice;
import com.webempresarial.store.config.SecurityConfig;
import com.webempresarial.store.config.SecurityViewAdvice;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.PilotOffer;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.CustomerTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.PilotAcceptanceChannel;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.service.PilotOfferService;
import com.webempresarial.store.theme.ThemeModelAdvice;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(
    controllers = PilotOfferAdminController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                FeatureViewAdvice.class,
                StoreViewAdvice.class,
                SecurityViewAdvice.class,
                ThemeModelAdvice.class
            }
        )
    }
)
@Import(SecurityConfig.class)
class PilotOfferAdminSecurityMockMvcTest {

    private static final String BASE =
            "/api/admin/platform/pilot-offers/stores/7";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PilotOfferService pilotOfferService;

    @MockitoBean
    private AdminAuthenticationSuccessHandler
            adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private CustomerTenantAccessInterceptor
            customerTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    @BeforeEach
    void allowMvcInterceptors() throws Exception {
        when(adminTenantAccessInterceptor.preHandle(
                any(), any(), any()
        )).thenReturn(true);

        when(subscriptionInterceptor.preHandle(
                any(), any(), any()
        )).thenReturn(true);
    }

    private PilotOffer mockOffer(PilotOfferStatus status) {
        PilotOffer offer = mock(PilotOffer.class);
        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(offer.getId()).thenReturn(9L);
        when(offer.getStore()).thenReturn(store);
        when(offer.getLead()).thenReturn(lead);

        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);

        when(offer.getCohort()).thenReturn(PilotCohort.A);
        when(offer.getStatus()).thenReturn(status);

        when(offer.getInitialAmountMxn())
                .thenReturn(new BigDecimal("990.00"));

        when(offer.getRenewalAmountMxn())
                .thenReturn(new BigDecimal("790.00"));

        when(offer.getCurrency()).thenReturn("MXN");
        when(offer.getTermsVersion())
                .thenReturn("pricing-v0.1");

        return offer;
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanListOffers() throws Exception {
        when(pilotOfferService.listForStore(7L))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(pilotOfferService).listForStore(7L);
    }

    @Test
    @WithMockUser(roles = "STORE_ADMIN")
    void storeAdminCannotListOffers() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    void anonymousUserIsRedirectedToAdminLogin()
            throws Exception {

        mockMvc.perform(get(BASE))
                .andExpect(status().is3xxRedirection())
                .andExpect(
                    redirectedUrlPattern("**/admin/login")
                );

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void postWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(
                post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"leadId\":11,\"cohort\":\"A\"}"
                    )
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(roles = "STORE_ADMIN")
    void storeAdminCannotCreateWithCsrf() throws Exception {
        mockMvc.perform(
                post(BASE)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"leadId\":11,\"cohort\":\"A\"}"
                    )
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(
        username = "root@platform.test",
        roles = "SUPER_ADMIN"
    )
    void superAdminCreatesDraftWithCsrf() throws Exception {
        PilotOffer preparedOffer =
                mockOffer(PilotOfferStatus.DRAFT);

        when(pilotOfferService.createDraft(
                7L, 11L, PilotCohort.A
        )).thenReturn(preparedOffer);

        mockMvc.perform(
                post(BASE)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"leadId\":11,\"cohort\":\"A\"}"
                    )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(9))
        .andExpect(jsonPath("$.initialAmountMxn").value(990.00))
        .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(pilotOfferService).createDraft(
                7L, 11L, PilotCohort.A
        );
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void invalidDraftPayloadIsRejected() throws Exception {
        mockMvc.perform(
                post(BASE)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"cohort\":\"A\"}")
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(
        username = "root@platform.test",
        roles = "SUPER_ADMIN"
    )
    void acceptanceUsesAuthenticatedIdentity()
            throws Exception {

        PilotOffer preparedOffer =
                mockOffer(PilotOfferStatus.ACCEPTED);

        when(pilotOfferService.recordAcceptance(
                7L,
                9L,
                PilotAcceptanceChannel.EMAIL,
                "email-message-123",
                "root@platform.test"
        )).thenReturn(preparedOffer);

        mockMvc.perform(
                post(BASE + "/9/accept")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"channel\":\"EMAIL\","
                        + "\"evidenceReference\":"
                        + "\"email-message-123\"}"
                    )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(pilotOfferService).recordAcceptance(
                7L,
                9L,
                PilotAcceptanceChannel.EMAIL,
                "email-message-123",
                "root@platform.test"
        );
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void offerOutsideRequestedStoreReturns404()
            throws Exception {

        when(pilotOfferService.getForStore(7L, 99L))
                .thenThrow(new EntityNotFoundException(
                    "Oferta no encontrada en esta tienda"
                ));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound());

        verify(pilotOfferService).getForStore(7L, 99L);
    }
}

package com.webempresarial.store.controller.admin.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
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
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.PilotPaymentMethod;
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
class PilotOfferPaymentAdminSecurityMockMvcTest {

    private static final String BASE =
            "/api/admin/platform/pilot-offers/stores/7/9";

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
        when(offer.getStatus()).thenReturn(status);
        when(offer.getPaymentMethod())
                .thenReturn(PilotPaymentMethod.TRANSFER);

        if (status == PilotOfferStatus.PAID
                || status == PilotOfferStatus.ACTIVE) {
            when(offer.getPaymentEvidenceReference())
                    .thenReturn("BANK:TEST:TX-123");

            when(offer.getPaymentVerifiedBy())
                    .thenReturn("platform-admin@test.local");
        }

        if (status == PilotOfferStatus.ACTIVE) {
            when(offer.getPilotStartedAt()).thenReturn(
                    LocalDateTime.of(2026, 11, 7, 12, 0)
            );

            when(offer.getPilotEndsAt()).thenReturn(
                    LocalDateTime.of(2026, 12, 7, 12, 0)
            );
        }

        return offer;
    }

    private String confirmationBody() {
        return "{\"bankTransactionReference\":"
                + "\"BANK:TEST:TX-123\"}";
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminStartsTransferWithCsrf() throws Exception {
        PilotOffer offer =
                mockOffer(PilotOfferStatus.PAYMENT_PENDING);

        when(pilotOfferService.beginTransferPayment(7L, 9L))
                .thenReturn(offer);

        mockMvc.perform(
                post(BASE + "/transfer/start").with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.status").value("PAYMENT_PENDING")
        )
        .andExpect(
                jsonPath("$.paymentMethod").value("TRANSFER")
        );

        verify(pilotOfferService).beginTransferPayment(7L, 9L);
    }

    @Test
    @WithMockUser(roles = "STORE_ADMIN")
    void storeAdminCannotStartTransfer() throws Exception {
        mockMvc.perform(
                post(BASE + "/transfer/start").with(csrf())
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void cannotStartTransferWithoutCsrf() throws Exception {
        mockMvc.perform(post(BASE + "/transfer/start"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    void anonymousUserCannotStartTransfer() throws Exception {
        mockMvc.perform(
                post(BASE + "/transfer/start").with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
                redirectedUrlPattern("**/admin/login")
        );

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(
        username = "platform-admin@test.local",
        roles = "SUPER_ADMIN"
    )
    void confirmationUsesAuthenticatedAdministrator()
            throws Exception {

        PilotOffer offer = mockOffer(PilotOfferStatus.PAID);

        when(pilotOfferService.confirmTransferPayment(
                7L,
                9L,
                "BANK:TEST:TX-123",
                "platform-admin@test.local"
        )).thenReturn(offer);

        mockMvc.perform(
                post(BASE + "/transfer/confirm")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(confirmationBody())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PAID"))
        .andExpect(
                jsonPath("$.paymentVerifiedBy")
                    .value("platform-admin@test.local")
        );

        verify(pilotOfferService).confirmTransferPayment(
                7L,
                9L,
                "BANK:TEST:TX-123",
                "platform-admin@test.local"
        );
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void confirmationRequiresBankReference() throws Exception {
        mockMvc.perform(
                post(BASE + "/transfer/confirm")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"bankTransactionReference\":\" \"}"
                    )
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(roles = "STORE_ADMIN")
    void storeAdminCannotConfirmPayment() throws Exception {
        mockMvc.perform(
                post(BASE + "/transfer/confirm")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(confirmationBody())
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void paymentConfirmationRequiresCsrf() throws Exception {
        mockMvc.perform(
                post(BASE + "/transfer/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(confirmationBody())
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(pilotOfferService);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void duplicateBankReferenceReturns409() throws Exception {
        when(pilotOfferService.confirmTransferPayment(
                eq(7L),
                eq(9L),
                eq("BANK:TEST:TX-123"),
                any(String.class)
        )).thenThrow(
                new DataIntegrityViolationException(
                    "Duplicate payment reference"
                )
        );

        mockMvc.perform(
                post(BASE + "/transfer/confirm")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(confirmationBody())
        )
        .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminActivatesPaidPilot() throws Exception {
        PilotOffer offer = mockOffer(PilotOfferStatus.ACTIVE);

        when(pilotOfferService.activatePilot(7L, 9L))
                .thenReturn(offer);

        mockMvc.perform(
                post(BASE + "/activate").with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(
                jsonPath("$.pilotEndsAt")
                    .value("2026-12-07T12:00:00")
        );

        verify(pilotOfferService).activatePilot(7L, 9L);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void invalidActivationReturns409() throws Exception {
        when(pilotOfferService.activatePilot(7L, 9L))
                .thenThrow(
                    new IllegalStateException("Pago no confirmado")
                );

        mockMvc.perform(
                post(BASE + "/activate").with(csrf())
        )
        .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void anotherTenantOfferReturns404() throws Exception {
        when(pilotOfferService.activatePilot(7L, 9L))
                .thenThrow(
                    new EntityNotFoundException(
                        "Oferta no encontrada"
                    )
                );

        mockMvc.perform(
                post(BASE + "/activate").with(csrf())
        )
        .andExpect(status().isNotFound());
    }
}

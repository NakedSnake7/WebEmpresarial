package com.webempresarial.store.controller.admin.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.PilotOffer;
import com.webempresarial.store.model.PilotAcceptanceChannel;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.PilotOfferService;

class PilotOfferAdminControllerTest {

    private Authentication admin() {
        return new UsernamePasswordAuthenticationToken(
                "superadmin@example.test",
                "unused",
                List.of(
                    new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")
                )
        );
    }

    private Authentication storeAdmin() {
        return new UsernamePasswordAuthenticationToken(
                "storeadmin@example.test",
                "unused",
                List.of(
                    new SimpleGrantedAuthority("ROLE_STORE_ADMIN")
                )
        );
    }

    private PilotOffer mockOffer() {
        PilotOffer offer = mock(PilotOffer.class);
        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(offer.getId()).thenReturn(9L);
        when(offer.getStore()).thenReturn(store);
        when(offer.getLead()).thenReturn(lead);
        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);
        when(offer.getCohort()).thenReturn(PilotCohort.A);
        when(offer.getStatus()).thenReturn(PilotOfferStatus.DRAFT);
        when(offer.getInitialAmountMxn())
                .thenReturn(new BigDecimal("990.00"));
        when(offer.getRenewalAmountMxn())
                .thenReturn(new BigDecimal("790.00"));
        when(offer.getCurrency()).thenReturn("MXN");
        when(offer.getTermsVersion()).thenReturn("pricing-v0.1");

        return offer;
    }

    @Test
    void createsDraftWithBackendPrice() {
        PilotOfferService service = mock(PilotOfferService.class);

        PilotOffer preparedOffer = mockOffer();

        when(service.createDraft(7L, 11L, PilotCohort.A))
                .thenReturn(preparedOffer);

        var controller = new PilotOfferAdminController(service);

        var result = controller.createDraft(
                7L,
                new PilotOfferAdminController.DraftRequest(
                        11L, PilotCohort.A
                ),
                admin()
        );

        assertEquals(201, result.getStatusCode().value());
        assertNotNull(result.getBody());

        assertEquals(
                new BigDecimal("990.00"),
                result.getBody().initialAmountMxn()
        );

        verify(service).createDraft(7L, 11L, PilotCohort.A);
    }

    @Test
    void acceptanceUsesAuthenticatedAdministratorIdentity() {
        PilotOfferService service = mock(PilotOfferService.class);

        PilotOffer preparedOffer = mockOffer();

        when(service.recordAcceptance(
                eq(7L),
                eq(9L),
                eq(PilotAcceptanceChannel.EMAIL),
                eq("email-message-123"),
                eq("superadmin@example.test")
        )).thenReturn(preparedOffer);

        var controller = new PilotOfferAdminController(service);

        controller.accept(
                7L,
                9L,
                new PilotOfferAdminController.AcceptanceRequest(
                        PilotAcceptanceChannel.EMAIL,
                        "email-message-123"
                ),
                admin()
        );

        verify(service).recordAcceptance(
                7L,
                9L,
                PilotAcceptanceChannel.EMAIL,
                "email-message-123",
                "superadmin@example.test"
        );
    }

    @Test
    void rejectsStoreAdminBeforeAccessingService() {
        PilotOfferService service = mock(PilotOfferService.class);

        var controller = new PilotOfferAdminController(service);

        assertThrows(
                AccessDeniedException.class,
                () -> controller.createDraft(
                        7L,
                        new PilotOfferAdminController.DraftRequest(
                                11L, PilotCohort.B
                        ),
                        storeAdmin()
                )
        );

        verifyNoInteractions(service);
    }

    @Test
    void rejectsMissingAuthentication() {
        PilotOfferService service = mock(PilotOfferService.class);

        var controller = new PilotOfferAdminController(service);

        assertThrows(
                AccessDeniedException.class,
                () -> controller.list(7L, null)
        );

        verifyNoInteractions(service);
    }

    @Test
    void publicationSuppliesFutureExpiryToService() {
        PilotOfferService service = mock(PilotOfferService.class);

        PilotOffer preparedOffer = mockOffer();

        when(service.publish(
                eq(7L),
                eq(9L),
                any(LocalDateTime.class)
        )).thenReturn(preparedOffer);

        var controller = new PilotOfferAdminController(service);

        LocalDateTime before = LocalDateTime.now();

        controller.publish(
                7L,
                9L,
                new PilotOfferAdminController.PublishRequest(7),
                admin()
        );

        verify(service).publish(
                eq(7L),
                eq(9L),
                argThat(expiry ->
                    expiry != null
                    && expiry.isAfter(before.plusDays(6))
                    && expiry.isBefore(
                            LocalDateTime.now().plusDays(8)
                    )
                )
        );
    }

    @Test
    void rejectsInvalidPublicationValidity() {
        PilotOfferService service = mock(PilotOfferService.class);
        var controller = new PilotOfferAdminController(service);

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.publish(
                        7L,
                        9L,
                        new PilotOfferAdminController.PublishRequest(31),
                        admin()
                )
        );

        verifyNoInteractions(service);
    }
}

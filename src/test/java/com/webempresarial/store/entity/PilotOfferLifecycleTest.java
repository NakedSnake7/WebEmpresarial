package com.webempresarial.store.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.Store;

class PilotOfferLifecycleTest {

    private PilotOffer draft() {
        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);
        when(lead.getStore()).thenReturn(store);

        return PilotOffer.draft(
                store,
                lead,
                PilotCohort.A
        );
    }

    @Test
    void draftUsesBackendPrices() {
        PilotOffer offer = draft();

        assertEquals(PilotOfferStatus.DRAFT, offer.getStatus());
        assertEquals(
                new BigDecimal("990.00"),
                offer.getInitialAmountMxn()
        );
        assertEquals(
                new BigDecimal("790.00"),
                offer.getRenewalAmountMxn()
        );
        assertEquals("MXN", offer.getCurrency());
    }

    @Test
    void draftCanBePublishedOnlyOnce() {
        PilotOffer offer = draft();

        LocalDateTime now =
                LocalDateTime.of(2026, 11, 7, 17, 0);

        LocalDateTime expiry = now.plusDays(7);

        offer.markOffered(now, expiry);

        assertEquals(PilotOfferStatus.OFFERED, offer.getStatus());
        assertEquals(now, offer.getOfferedAt());
        assertEquals(expiry, offer.getExpiresAt());

        assertThrows(
                IllegalStateException.class,
                () -> offer.markOffered(now, expiry)
        );
    }

    @Test
    void cannotPublishAlreadyExpiredOffer() {
        PilotOffer offer = draft();

        LocalDateTime now =
                LocalDateTime.of(2026, 11, 7, 17, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> offer.markOffered(now, now.minusMinutes(1))
        );

        assertEquals(PilotOfferStatus.DRAFT, offer.getStatus());
    }

    @Test
    void cannotCreateOfferForAnotherTenant() {
        Store store = mock(Store.class);
        Store other = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(store.getId()).thenReturn(7L);
        when(other.getId()).thenReturn(8L);
        when(lead.getId()).thenReturn(11L);
        when(lead.getStore()).thenReturn(other);

        assertThrows(
                IllegalArgumentException.class,
                () -> PilotOffer.draft(
                        store,
                        lead,
                        PilotCohort.B
                )
        );
    }
}

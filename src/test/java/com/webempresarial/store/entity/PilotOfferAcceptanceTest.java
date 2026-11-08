package com.webempresarial.store.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.webempresarial.store.model.PilotAcceptanceChannel;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.Store;

class PilotOfferAcceptanceTest {

    private final LocalDateTime now =
            LocalDateTime.of(2026, 11, 7, 17, 0);

    private PilotOffer offered() {
        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);
        when(lead.getStore()).thenReturn(store);

        PilotOffer offer =
                PilotOffer.draft(store, lead, PilotCohort.B);

        offer.markOffered(now, now.plusDays(7));
        return offer;
    }

    @Test
    void recordsEvidenceAndAcceptedPrices() {
        PilotOffer offer = offered();

        offer.recordAcceptance(
                PilotAcceptanceChannel.EMAIL,
                "email-message-reference-123",
                "admin@example.test",
                now.plusDays(1)
        );

        assertEquals(
                PilotOfferStatus.ACCEPTED, offer.getStatus()
        );
        assertEquals(
                PilotAcceptanceChannel.EMAIL,
                offer.getAcceptanceChannel()
        );
        assertEquals(
                "email-message-reference-123",
                offer.getAcceptanceReference()
        );
        assertEquals(
                "admin@example.test",
                offer.getAcceptanceRecordedBy()
        );
        assertEquals("pricing-v0.1",
                offer.getAcceptedTermsVersion());
        assertEquals(
                new BigDecimal("1490.00"),
                offer.getAcceptedInitialAmountMxn()
        );
        assertEquals(
                new BigDecimal("790.00"),
                offer.getAcceptedRenewalAmountMxn()
        );
        assertEquals(now.plusDays(1), offer.getAcceptedAt());
    }

    @Test
    void rejectsExpiredOffer() {
        PilotOffer offer = offered();

        assertThrows(
                IllegalStateException.class,
                () -> offer.recordAcceptance(
                        PilotAcceptanceChannel.WHATSAPP,
                        "message-123",
                        "admin@example.test",
                        now.plusDays(7)
                )
        );

        assertEquals(
                PilotOfferStatus.OFFERED, offer.getStatus()
        );
    }

    @Test
    void rejectsMissingEvidence() {
        PilotOffer offer = offered();

        assertThrows(
                IllegalArgumentException.class,
                () -> offer.recordAcceptance(
                        PilotAcceptanceChannel.EMAIL,
                        " ",
                        "admin@example.test",
                        now.plusDays(1)
                )
        );

        assertEquals(
                PilotOfferStatus.OFFERED, offer.getStatus()
        );
    }

    @Test
    void rejectsMissingRecorder() {
        PilotOffer offer = offered();

        assertThrows(
                IllegalArgumentException.class,
                () -> offer.recordAcceptance(
                        PilotAcceptanceChannel.EMAIL,
                        "message-123",
                        " ",
                        now.plusDays(1)
                )
        );
    }

    @Test
    void cannotAcceptDraft() {
        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);
        when(lead.getStore()).thenReturn(store);

        PilotOffer draft =
                PilotOffer.draft(store, lead, PilotCohort.A);

        assertThrows(
                IllegalStateException.class,
                () -> draft.recordAcceptance(
                        PilotAcceptanceChannel.EMAIL,
                        "message-123",
                        "admin@example.test",
                        now
                )
        );
    }

    @Test
    void cannotAcceptTwice() {
        PilotOffer offer = offered();

        offer.recordAcceptance(
                PilotAcceptanceChannel.EMAIL,
                "message-123",
                "admin@example.test",
                now.plusDays(1)
        );

        assertThrows(
                IllegalStateException.class,
                () -> offer.recordAcceptance(
                        PilotAcceptanceChannel.EMAIL,
                        "message-456",
                        "admin@example.test",
                        now.plusDays(2)
                )
        );
    }
}

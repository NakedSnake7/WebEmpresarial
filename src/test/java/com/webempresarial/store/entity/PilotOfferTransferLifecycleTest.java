package com.webempresarial.store.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.webempresarial.store.model.PilotAcceptanceChannel;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.PilotPaymentMethod;
import com.webempresarial.store.model.Store;

class PilotOfferTransferLifecycleTest {

    private final LocalDateTime now =
            LocalDateTime.of(2026, 11, 7, 18, 0);

    private PilotOffer acceptedOffer() {
        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);
        when(lead.getStore()).thenReturn(store);

        PilotOffer offer = PilotOffer.draft(
                store, lead, PilotCohort.B
        );

        offer.markOffered(
                now.minusHours(1),
                now.plusDays(7)
        );

        offer.recordAcceptance(
                PilotAcceptanceChannel.EMAIL,
                "acceptance-message-1",
                "root@platform.test",
                now
        );

        return offer;
    }

    private PilotOffer pendingOffer() {
        PilotOffer offer = acceptedOffer();
        offer.beginTransferPayment();
        return offer;
    }

    private PilotOffer paidOffer() {
        PilotOffer offer = pendingOffer();

        offer.confirmTransferPayment(
                "BANK:MX:TX-000123",
                "root@platform.test",
                now.plusHours(2)
        );

        return offer;
    }

    @Test
    void beginsTransferWithoutMarkingPaid() {
        PilotOffer offer = acceptedOffer();

        offer.beginTransferPayment();

        assertEquals(
                PilotOfferStatus.PAYMENT_PENDING,
                offer.getStatus()
        );

        assertEquals(
                PilotPaymentMethod.TRANSFER,
                offer.getPaymentMethod()
        );

        assertNull(offer.getPaidAt());

        // Repetir no altera el estado.
        offer.beginTransferPayment();

        assertEquals(
                PilotOfferStatus.PAYMENT_PENDING,
                offer.getStatus()
        );
    }

    @Test
    void rejectsTransferWithoutAcceptance() {
        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);
        when(lead.getStore()).thenReturn(store);

        PilotOffer draft =
                PilotOffer.draft(store, lead, PilotCohort.A);

        assertThrows(
                IllegalStateException.class,
                draft::beginTransferPayment
        );
    }

    @Test
    void recordsVerifiedTransferEvidence() {
        PilotOffer offer = paidOffer();

        assertEquals(
                PilotOfferStatus.PAID,
                offer.getStatus()
        );

        assertEquals(
                "BANK:MX:TX-000123",
                offer.getPaymentEvidenceReference()
        );

        assertEquals(
                "root@platform.test",
                offer.getPaymentVerifiedBy()
        );

        assertEquals(
                now.plusHours(2),
                offer.getPaidAt()
        );

        assertEquals(
                now.plusHours(2),
                offer.getPaymentVerifiedAt()
        );
    }

    @Test
    void repeatedConfirmationIsIdempotent() {
        PilotOffer offer = paidOffer();

        LocalDateTime originalPaidAt = offer.getPaidAt();

        offer.confirmTransferPayment(
                "BANK:MX:TX-000123",
                "another-admin@test.local",
                now.plusDays(1)
        );

        assertEquals(originalPaidAt, offer.getPaidAt());

        assertEquals(
                "root@platform.test",
                offer.getPaymentVerifiedBy()
        );
    }

    @Test
    void rejectsDifferentConfirmationReference() {
        PilotOffer offer = paidOffer();

        assertThrows(
                IllegalStateException.class,
                () -> offer.confirmTransferPayment(
                        "BANK:MX:OTHER-REF",
                        "root@platform.test",
                        now.plusDays(1)
                )
        );
    }

    @Test
    void rejectsBlankTransferEvidence() {
        PilotOffer offer = pendingOffer();

        assertThrows(
                IllegalArgumentException.class,
                () -> offer.confirmTransferPayment(
                        " ",
                        "root@platform.test",
                        now.plusHours(1)
                )
        );

        assertEquals(
                PilotOfferStatus.PAYMENT_PENDING,
                offer.getStatus()
        );
    }

    @Test
    void activationStartsThirtyDayPeriod() {
        PilotOffer offer = paidOffer();

        LocalDateTime activation = now.plusDays(2);

        offer.activatePilot(activation);

        assertEquals(
                PilotOfferStatus.ACTIVE,
                offer.getStatus()
        );

        assertEquals(
                activation,
                offer.getPilotStartedAt()
        );

        assertEquals(
                activation.plusDays(30),
                offer.getPilotEndsAt()
        );

        // No debe reiniciar el periodo ante un reintento.
        offer.activatePilot(activation.plusDays(1));

        assertEquals(
                activation.plusDays(30),
                offer.getPilotEndsAt()
        );
    }

    @Test
    void cannotActivateUnpaidOffer() {
        PilotOffer offer = pendingOffer();

        assertThrows(
                IllegalStateException.class,
                () -> offer.activatePilot(now.plusDays(1))
        );
    }
}

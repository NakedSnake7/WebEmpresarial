package com.webempresarial.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.PilotOffer;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.PilotOfferRepository;

import jakarta.persistence.EntityNotFoundException;

class PilotOfferServiceTest {

    @Test
    void createsDraftOnlyForLeadInRequestedStore() {
        PilotOfferRepository offers =
                mock(PilotOfferRepository.class);

        LeadRepository leads =
                mock(LeadRepository.class);

        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);
        when(lead.getStore()).thenReturn(store);

        when(leads.findByIdAndStoreId(11L, 7L))
                .thenReturn(Optional.of(lead));

        when(offers.save(any(PilotOffer.class)))
                .thenAnswer(i -> i.getArgument(0));

        PilotOfferService service =
                new PilotOfferService(offers, leads);

        PilotOffer result = service.createDraft(
                7L,
                11L,
                PilotCohort.B
        );

        assertEquals(PilotCohort.B, result.getCohort());
        assertEquals(PilotOfferStatus.DRAFT, result.getStatus());

        verify(leads).findByIdAndStoreId(11L, 7L);
        verify(offers).save(any(PilotOffer.class));
    }

    @Test
    void rejectsLeadOutsideRequestedStore() {
        PilotOfferRepository offers =
                mock(PilotOfferRepository.class);

        LeadRepository leads =
                mock(LeadRepository.class);

        when(leads.findByIdAndStoreId(11L, 7L))
                .thenReturn(Optional.empty());

        PilotOfferService service =
                new PilotOfferService(offers, leads);

        assertThrows(
                EntityNotFoundException.class,
                () -> service.createDraft(
                        7L, 11L, PilotCohort.A
                )
        );

        verify(offers, never()).save(any(PilotOffer.class));
    }

    @Test
    void publishesOnlyStoreScopedOffer() {
        PilotOfferRepository offers =
                mock(PilotOfferRepository.class);

        LeadRepository leads =
                mock(LeadRepository.class);

        Store store = mock(Store.class);
        Lead lead = mock(Lead.class);

        when(store.getId()).thenReturn(7L);
        when(lead.getId()).thenReturn(11L);
        when(lead.getStore()).thenReturn(store);

        PilotOffer draft = PilotOffer.draft(
                store, lead, PilotCohort.A
        );

        when(offers.findByIdAndStore_Id(21L, 7L))
                .thenReturn(Optional.of(draft));

        PilotOfferService service =
                new PilotOfferService(offers, leads);

        PilotOffer result = service.publish(
                7L,
                21L,
                LocalDateTime.now().plusDays(7)
        );

        assertEquals(PilotOfferStatus.OFFERED, result.getStatus());
        assertNotNull(result.getOfferedAt());
        assertNotNull(result.getExpiresAt());
    }
}

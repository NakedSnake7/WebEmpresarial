package com.webempresarial.store.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.PilotOffer;
import com.webempresarial.store.model.PilotAcceptanceChannel;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.PilotOfferRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PilotOfferService {

    private final PilotOfferRepository pilotOfferRepository;
    private final LeadRepository leadRepository;

    public PilotOfferService(
            PilotOfferRepository pilotOfferRepository,
            LeadRepository leadRepository
    ) {
        this.pilotOfferRepository = pilotOfferRepository;
        this.leadRepository = leadRepository;
    }

    @Transactional
    public PilotOffer createDraft(
            Long storeId,
            Long leadId,
            PilotCohort cohort
    ) {
        requirePositive(storeId, "storeId");
        requirePositive(leadId, "leadId");
        Objects.requireNonNull(cohort, "Cohorte obligatoria");

        Lead lead = leadRepository.findByIdAndStoreId(
                leadId,
                storeId
        ).orElseThrow(() -> new EntityNotFoundException(
                "Lead no encontrado en la tienda indicada"
        ));

        PilotOffer offer = PilotOffer.draft(
                lead.getStore(),
                lead,
                cohort
        );

        return pilotOfferRepository.save(offer);
    }

    @Transactional
    public PilotOffer publish(
            Long storeId,
            Long offerId,
            LocalDateTime expiresAt
    ) {
        PilotOffer offer = getForStore(storeId, offerId);

        offer.markOffered(
                LocalDateTime.now(),
                expiresAt
        );

        // Dirty checking JPA; no requiere save adicional.
        return offer;
    }


    /**
     * Solo debe invocarse desde una operación administrativa
     * autorizada, después de verificar la evidencia externa.
     */
    @Transactional
    public PilotOffer recordAcceptance(
            Long storeId,
            Long offerId,
            PilotAcceptanceChannel channel,
            String evidenceReference,
            String recordedBy
    ) {
        PilotOffer offer = getForStore(storeId, offerId);

        offer.recordAcceptance(
                channel,
                evidenceReference,
                recordedBy,
                LocalDateTime.now()
        );

        return offer;
    }


    @Transactional
    public PilotOffer beginTransferPayment(
            Long storeId,
            Long offerId
    ) {
        PilotOffer offer = getForStore(storeId, offerId);
        offer.beginTransferPayment();
        return offer;
    }

    /**
     * Uso interno tras conciliación bancaria verificada.
     * Nunca debe llamarse desde un endpoint público.
     */
    @Transactional
    public PilotOffer confirmTransferPayment(
            Long storeId,
            Long offerId,
            String bankTransactionReference,
            String verifiedBy
    ) {
        PilotOffer offer = getForStore(storeId, offerId);

        offer.confirmTransferPayment(
                bankTransactionReference,
                verifiedBy,
                LocalDateTime.now()
        );

        return offer;
    }

    @Transactional
    public PilotOffer activatePilot(
            Long storeId,
            Long offerId
    ) {
        PilotOffer offer = getForStore(storeId, offerId);
        offer.activatePilot(LocalDateTime.now());
        return offer;
    }

    @Transactional(readOnly = true)
    public PilotOffer getForStore(
            Long storeId,
            Long offerId
    ) {
        requirePositive(storeId, "storeId");
        requirePositive(offerId, "offerId");

        return pilotOfferRepository.findByIdAndStore_Id(
                offerId,
                storeId
        ).orElseThrow(() -> new EntityNotFoundException(
                "Oferta no encontrada en la tienda indicada"
        ));
    }

    @Transactional(readOnly = true)
    public List<PilotOffer> listForStore(Long storeId) {
        requirePositive(storeId, "storeId");

        return pilotOfferRepository
                .findByStore_IdOrderByCreatedAtDesc(storeId);
    }

    private void requirePositive(Long value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    name + " debe ser positivo"
            );
        }
    }
}

package com.webempresarial.store.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.webempresarial.store.dto.lead.CreateProposalDTO;
import com.webempresarial.store.dto.lead.ProposalDTO;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.Proposal;
import com.webempresarial.store.model.ActivityType;
import com.webempresarial.store.model.LeadStatus;
import com.webempresarial.store.model.ProposalStatus;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.ProposalRepository;

import jakarta.transaction.Transactional;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final LeadRepository leadRepository;
    private final LeadActivityService activityService;
    private final AutomationService automationService;

    public ProposalService(
            ProposalRepository proposalRepository,
            LeadRepository leadRepository,
            LeadActivityService activityService,
            AutomationService automationService
    ) {
        this.proposalRepository = proposalRepository;
        this.leadRepository = leadRepository;
        this.activityService = activityService;
        this.automationService = automationService;
    }

    public List<ProposalDTO> getByLead(Long leadId, Long storeId) {
        Lead lead = leadRepository.findByIdAndStoreId(leadId, storeId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado para esta tienda"));

        return proposalRepository
                .findByLeadIdAndLeadStoreIdOrderByCreatedAtDesc(
                        leadId,
                        storeId
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public ProposalDTO createProposal(
            Long leadId,
            Long storeId,
            CreateProposalDTO dto
    ) {
        Lead lead = leadRepository.findByIdAndStoreId(leadId, storeId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado para esta tienda"));

        Proposal proposal = new Proposal();

        proposal.setLead(lead);
        proposal.setTitle(dto.title().trim());
        proposal.setDescription(dto.description());
        proposal.setAmount(dto.amount());
        proposal.setCloseProbability(
                dto.closeProbability() == null
                        ? 50
                        : dto.closeProbability()
        );
        proposal.setStatus(ProposalStatus.DRAFT);

        Proposal saved = proposalRepository.save(proposal);

        activityService.log(
                lead,
                ActivityType.PROPOSAL_CREATED,
                "Propuesta creada",
                saved.getTitle()
        );

        return toDTO(saved);
    }

    @Transactional
    public ProposalDTO sendProposal(Long proposalId, Long storeId) {

        Proposal proposal = proposalRepository.findByIdAndLeadStoreId(proposalId, storeId)
                .orElseThrow(() -> new RuntimeException("Propuesta no encontrada para esta tienda"));

        proposal.setStatus(ProposalStatus.SENT);
        proposal.setSentAt(LocalDateTime.now());

        Lead lead = proposal.getLead();
        lead.setStatus(LeadStatus.PROPOSAL_SENT);
        lead.setUpdatedAt(LocalDateTime.now());

        Proposal saved = proposalRepository.save(proposal);

        activityService.log(
                lead,
                ActivityType.PROPOSAL_SENT,
                "Propuesta enviada",
                saved.getTitle()
        );

        automationService.onProposalSent(lead);

        return toDTO(saved);
    }

    @Transactional
    public ProposalDTO acceptProposal(Long proposalId, Long storeId) {

        Proposal proposal = proposalRepository.findByIdAndLeadStoreId(proposalId, storeId)
                .orElseThrow(() -> new RuntimeException("Propuesta no encontrada para esta tienda"));

        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setAcceptedAt(LocalDateTime.now());

        Lead lead = proposal.getLead();
        lead.setStatus(LeadStatus.CLOSED);
        lead.setClosedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());

        activityService.log(
                lead,
                ActivityType.PROPOSAL_ACCEPTED,
                "Propuesta aceptada",
                proposal.getTitle()
        );

        return toDTO(proposalRepository.save(proposal));
    }

    @Transactional
    public ProposalDTO rejectProposal(Long proposalId, Long storeId) {

        Proposal proposal = proposalRepository.findByIdAndLeadStoreId(proposalId, storeId)
                .orElseThrow(() -> new RuntimeException("Propuesta no encontrada para esta tienda"));

        proposal.setStatus(ProposalStatus.REJECTED);
        proposal.setRejectedAt(LocalDateTime.now());

        activityService.log(
                proposal.getLead(),
                ActivityType.PROPOSAL_REJECTED,
                "Propuesta rechazada",
                proposal.getTitle()
        );

        return toDTO(proposalRepository.save(proposal));
    }

    private ProposalDTO toDTO(Proposal proposal) {
        return new ProposalDTO(
                proposal.getId(),
                proposal.getTitle(),
                proposal.getDescription(),
                proposal.getAmount(),
                proposal.getCloseProbability(),
                proposal.getStatus().name(),
                proposal.getCreatedAt(),
                proposal.getSentAt()
        );
    }
}
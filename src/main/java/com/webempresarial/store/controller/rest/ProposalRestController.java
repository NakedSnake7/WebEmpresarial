package com.webempresarial.store.controller.rest;

import java.util.List;  

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.webempresarial.store.service.ProposalPdfService;
import com.webempresarial.store.service.PlanFeatureService;
import com.webempresarial.store.exceptions.FeatureNotAllowedException;

import com.webempresarial.store.dto.lead.CreateProposalDTO;
import com.webempresarial.store.dto.lead.ProposalDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.ProposalService;
import com.webempresarial.store.service.StoreContextService;

@RestController
@RequestMapping("/api/crm")
public class ProposalRestController {

    private final ProposalService proposalService;
    private final StoreContextService storeContextService;
    private final ProposalPdfService proposalPdfService;
    private final PlanFeatureService planFeatureService;

    public ProposalRestController(
            ProposalService proposalService,
            StoreContextService storeContextService,
            ProposalPdfService proposalPdfService,
            PlanFeatureService planFeatureService

    ) {
        this.proposalService = proposalService;
        this.storeContextService = storeContextService;
        this.proposalPdfService = proposalPdfService;
        this.planFeatureService = planFeatureService;

    }
    @GetMapping("/leads/{leadId}/proposals")
    public List<ProposalDTO> getLeadProposals(
            @PathVariable Long leadId,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        validateProposalsFeature(store);

        return proposalService.getByLead(leadId, store.getId());
    }

    @PostMapping("/leads/{leadId}/proposals")
    public ResponseEntity<ProposalDTO> createProposal(
            @PathVariable Long leadId,
            @Valid @RequestBody CreateProposalDTO dto,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        validateProposalsFeature(store);

        return ResponseEntity.ok(
                proposalService.createProposal(leadId, store.getId(), dto)
        );
    }

    @PatchMapping("/proposals/{proposalId}/send")
    public ProposalDTO sendProposal(
            @PathVariable Long proposalId,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        validateProposalsFeature(store);

        return proposalService.sendProposal(proposalId, store.getId());
    }

    @PatchMapping("/proposals/{proposalId}/accept")
    public ProposalDTO acceptProposal(
            @PathVariable Long proposalId,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        validateProposalsFeature(store);

        return proposalService.acceptProposal(proposalId, store.getId());
    }

    @PatchMapping("/proposals/{proposalId}/reject")
    public ProposalDTO rejectProposal(
            @PathVariable Long proposalId,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        validateProposalsFeature(store);

        return proposalService.rejectProposal(proposalId, store.getId());
    }
    
    @GetMapping("/proposals/{proposalId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long proposalId,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        validateProposalsFeature(store);

        byte[] pdf = proposalPdfService.generateProposalPdf(
                proposalId,
                store.getId()
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=propuesta-" + proposalId + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    
    private void validateProposalsFeature(Store store) {
        if (!planFeatureService.canUseProposals(store)) {
            throw new FeatureNotAllowedException(
                    "Tu plan actual no incluye propuestas comerciales."
            );
        }
    }
    
}
package com.webempresarial.store.controller;

import com.webempresarial.store.dto.LeadRequestDTO;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.LeadService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;
    private final StoreResolver storeResolver;

    public LeadController(
            LeadService leadService,
            StoreResolver storeResolver
    ) {
        this.leadService = leadService;
        this.storeResolver = storeResolver;
    }

    @PostMapping
    public ResponseEntity<?> createLead(
            @Valid @RequestBody LeadRequestDTO dto,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        Lead lead = leadService.createLead(dto, store);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "success", true,
                        "id", lead.getId(),
                        "store", store.getNombre(),
                        "message", "Lead creado correctamente"
                ));
    }
}
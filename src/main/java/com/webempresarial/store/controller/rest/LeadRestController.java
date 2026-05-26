package com.webempresarial.store.controller.rest;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.dto.lead.CreateNoteDTO;
import com.webempresarial.store.dto.lead.CreateTaskDTO;
import com.webempresarial.store.dto.lead.LeadCardDTO;
import com.webempresarial.store.dto.lead.LeadDetailDTO;
import com.webempresarial.store.dto.lead.UpdateLeadStatusDTO;
import com.webempresarial.store.service.LeadService;
import com.webempresarial.store.service.SalesTaskService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/crm/leads")
public class LeadRestController {

    private final LeadService leadService;
    private final SalesTaskService taskService;
    private final StoreContextService storeContextService;

    public LeadRestController(
            LeadService leadService,
            SalesTaskService taskService,
            StoreContextService storeContextService
    ) {
        this.leadService = leadService;
        this.taskService = taskService;
        this.storeContextService = storeContextService;
    }

    @GetMapping
    public List<LeadCardDTO> getLeads(HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);
        return leadService.getLeadsForStore(store.getId());
    }

    @GetMapping("/{id}")
    public LeadDetailDTO getLead(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        return leadService.getDetail(id, store.getId());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateLeadStatusDTO dto
    ) {
        leadService.updateStatus(id, dto.status(), getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<Void> addNote(
            @PathVariable Long id,
            @RequestBody CreateNoteDTO dto
    ) {

        leadService.addNote(id, dto);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<Void> createTask(
            @PathVariable Long id,
            @RequestBody CreateTaskDTO dto
    ) {

        taskService.createManualTask(id, dto);

        return ResponseEntity.ok().build();
    }
    
    private AdminUser getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AdminUser adminUser) {
            return adminUser;
        }

        return null;
    }
}
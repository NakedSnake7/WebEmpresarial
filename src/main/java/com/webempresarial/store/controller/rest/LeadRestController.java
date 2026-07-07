package com.webempresarial.store.controller.rest;

import java.util.List;  
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.crm.merge.LeadMergeResult;
import com.webempresarial.store.crm.merge.LeadMergeService;
import com.webempresarial.store.dto.lead.CreateNoteDTO;
import com.webempresarial.store.dto.lead.CreateTaskDTO;
import com.webempresarial.store.dto.lead.LeadAuditLogDTO;
import com.webempresarial.store.dto.lead.LeadCardDTO;
import com.webempresarial.store.dto.lead.LeadDetailDTO;
import com.webempresarial.store.dto.lead.LeadScoreDTO;
import com.webempresarial.store.dto.lead.LeadTimelineItemDTO;
import com.webempresarial.store.dto.lead.MergeLeadDTO;
import com.webempresarial.store.dto.lead.UpdateLeadStatusDTO;
import com.webempresarial.store.service.LeadService;
import com.webempresarial.store.service.SalesTaskService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.crm.LeadAuditLogService;
import com.webempresarial.store.service.crm.LeadTimelineService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/crm/leads")
public class LeadRestController {

    private final LeadService leadService;
    private final SalesTaskService taskService;
    private final StoreContextService storeContextService;
    private final LeadAuditLogService leadAuditLogService;
    private final LeadTimelineService leadTimelineService;
    private final LeadMergeService leadMergeService;

    public LeadRestController(
            LeadService leadService,
            SalesTaskService taskService,
            StoreContextService storeContextService,
            LeadAuditLogService leadAuditLogService,
            LeadTimelineService leadTimelineService,
            LeadMergeService leadMergeService
    ) {
        this.leadService = leadService;
        this.taskService = taskService;
        this.storeContextService = storeContextService;
        this.leadAuditLogService = leadAuditLogService;
        this.leadTimelineService = leadTimelineService;
        this.leadMergeService = leadMergeService;
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
            @RequestBody UpdateLeadStatusDTO dto,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        leadService.updateStatus(
                id,
                store.getId(),
                dto.status(),
                getCurrentUser()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<Void> addNote(
            @PathVariable Long id,
            @RequestBody CreateNoteDTO dto,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        leadService.addNote(
                id,
                store.getId(),
                dto
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<Void> createTask(
            @PathVariable Long id,
            @RequestBody CreateTaskDTO dto,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        taskService.createManualTask(
                id,
                store.getId(),
                dto
        );

        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/audit")
    public List<LeadAuditLogDTO> getAudit(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        return leadAuditLogService.getByLead(
                id,
                store.getId()
        );
    }
    
    @GetMapping("/{id}/timeline")
    public List<LeadTimelineItemDTO> getTimeline(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        return leadTimelineService.getTimeline(
                id,
                store.getId()
        );
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
    @GetMapping("/{id}/score")
    public LeadScoreDTO getScore(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        return leadService.getScore(id, store.getId());
    }
    
    @PostMapping("/merge")
    public LeadMergeResult merge(
            @RequestBody MergeLeadDTO dto,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        AdminUser user = getCurrentUser();

        return leadMergeService.merge(
                dto.sourceLeadId(),
                dto.targetLeadId(),
                store.getId(),
                dto.strategy(),
                user != null ? user.getEmail() : "SYSTEM"
        );
    }
}
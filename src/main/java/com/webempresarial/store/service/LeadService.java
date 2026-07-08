 package com.webempresarial.store.service;

 import java.util.List;   
 import java.util.ArrayList;

 import com.webempresarial.store.dto.lead.CreateNoteDTO;
import com.webempresarial.store.dto.lead.LeadActivityDTO;
import com.webempresarial.store.dto.lead.LeadAuditLogDTO;
import com.webempresarial.store.dto.lead.LeadCardDTO;
 import com.webempresarial.store.dto.lead.LeadDetailDTO;
import com.webempresarial.store.dto.lead.LeadScoreDTO;
import com.webempresarial.store.dto.lead.LeadTimelineItemDTO;
import com.webempresarial.store.dto.lead.SalesTaskDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webempresarial.store.crm.duplicates.DuplicateCheckResult;
import com.webempresarial.store.crm.duplicates.DuplicateLeadException;
import com.webempresarial.store.crm.duplicates.LeadDuplicateService;
import com.webempresarial.store.crm.scoring.LeadScoreResult;
import com.webempresarial.store.crm.scoring.LeadScoringEngine;
import com.webempresarial.store.dto.LeadRequestDTO; 
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.events.LeadCreatedEvent;
import com.webempresarial.store.model.ActivityType;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.LeadStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.LeadActivityRepository;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.SalesTaskRepository;
import com.webempresarial.store.service.crm.LeadAuditLogService;
import com.webempresarial.store.service.crm.LeadTimelineService;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LeadActivityService activityService;
    private final LeadActivityRepository leadActivityRepository;
    private final SalesTaskRepository salesTaskRepository;
    private final LeadAuditLogService leadAuditLogService;
    private final LeadScoringEngine leadScoringEngine;
    private final LeadTimelineService leadTimelineService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final LeadDuplicateService leadDuplicateService;
    
    public LeadService(
            LeadRepository leadRepository,
            ApplicationEventPublisher eventPublisher,
            LeadActivityService activityService,
            LeadActivityRepository leadActivityRepository,
            SalesTaskRepository salesTaskRepository,
            LeadAuditLogService leadAuditLogService,
            LeadScoringEngine leadScoringEngine,
            LeadDuplicateService leadDuplicateService,
            LeadTimelineService leadTimelineService
    ) {
        this.leadRepository = leadRepository;
        this.eventPublisher = eventPublisher;
        this.activityService = activityService;
        this.leadActivityRepository = leadActivityRepository;
        this.salesTaskRepository = salesTaskRepository;
        this.leadAuditLogService = leadAuditLogService;
        this.leadScoringEngine = leadScoringEngine;
        this.leadDuplicateService = leadDuplicateService;
        this.leadTimelineService = leadTimelineService;
    }

    @Transactional
    public Lead createLead(
            LeadRequestDTO dto,
            Store store
    ) {
        String whatsapp = dto.getWhatsapp() != null
                ? dto.getWhatsapp().trim()
                : null;

        if (whatsapp == null || whatsapp.isBlank()) {
            throw new RuntimeException("El WhatsApp es obligatorio");
        }


        DuplicateCheckResult duplicate =
                leadDuplicateService.check(dto, store);

        if (duplicate.duplicated()) {
            throw new DuplicateLeadException(
                    duplicate.existingLead().getId(),
                    duplicate.reason()
            );
        }

        Lead lead = new Lead();

        lead.setStore(store);
        lead.setNombre(dto.getNombre().trim());
        lead.setWhatsapp(whatsapp);
        lead.setEmpresa(trimOrNull(dto.getEmpresa()));
        lead.setInstagram(trimOrNull(dto.getInstagram()));
        lead.setServicio(dto.getServicio() == null || dto.getServicio().isBlank()
                ? "Sin definir"
                : dto.getServicio().trim());
        lead.setPresupuesto(dto.getPresupuesto() == null || dto.getPresupuesto().isBlank()
                ? "Sin definir"
                : dto.getPresupuesto().trim());
        lead.setObjetivo(trimOrNull(dto.getObjetivo()));
        lead.setSource(trimOrDefault(dto.getSource(), "index"));

        recalculateScore(lead);

        Lead savedLead = leadRepository.save(lead);
        
        leadAuditLogService.record(
                savedLead,
                "LEAD_CREATED",
                "lead",
                null,
                savedLead.getNombre(),
                "SYSTEM"
        );
        
        activityService.log(
                savedLead,
                ActivityType.LEAD_CREATED,
                "Lead creado",
                "Lead capturado desde " + savedLead.getSource()
        );
        
        eventPublisher.publishEvent(
                new LeadCreatedEvent(savedLead)
        );

        return savedLead;
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String trimOrDefault(
            String value,
            String defaultValue
    ) {
        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }
    
    @Transactional
    public void updateStatus(
            Long leadId,
            Long storeId,
            LeadStatus newStatus,
            AdminUser user
    ) {

        Lead lead = leadRepository.findByIdAndStoreId(leadId, storeId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado para esta tienda"));

        LeadStatus oldStatus = lead.getStatus();

        if (oldStatus == newStatus) {
            return;
        }

        lead.setStatus(newStatus);
        lead.setUpdatedAt(LocalDateTime.now());

        if (newStatus == LeadStatus.CONTACTED) {
            lead.setLastContactAt(LocalDateTime.now());
        }

        if (newStatus == LeadStatus.FOLLOW_UP) {
            lead.setNextFollowUpAt(LocalDateTime.now().plusHours(24));
        }

        if (newStatus == LeadStatus.CLOSED) {
            lead.setClosedAt(LocalDateTime.now());
        }

        if (newStatus == LeadStatus.LOST) {
            lead.setLostAt(LocalDateTime.now());
        }

        leadRepository.save(lead);
        
        leadAuditLogService.record(
                lead,
                "STATUS_CHANGED",
                "status",
                oldStatus,
                newStatus,
                user != null ? user.getEmail() : "SYSTEM"
        );

        activityService.log(
                lead,
                ActivityType.STATUS_CHANGED,
                "Estado actualizado",
                oldStatus + " → " + newStatus
        );
    }




    @Transactional
    public void addNote(
            Long leadId,
            Long storeId,
            CreateNoteDTO dto
    ) {

        Lead lead = leadRepository.findByIdAndStoreId(leadId, storeId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado para esta tienda"));

        activityService.log(
                lead,
                ActivityType.NOTE_ADDED,
                "Nota agregada",
                dto.note()
        );
    }
    
    public List<LeadCardDTO> getLeadsForStore(Long storeId) {

        return leadRepository.findByStoreIdAndMergedFalseOrderByCreatedAtDesc(storeId)
                .stream()
                .map(lead -> new LeadCardDTO(
                        lead.getId(),
                        lead.getNombre(),
                        lead.getEmpresa(),
                        null,
                        lead.getWhatsapp(),
                        lead.getStatus().name(),
                        lead.getTemperature().name(),
                        lead.getPriority().name(),
                        lead.getScore(),
                        lead.getProjectedValue(),
                        lead.getSource(),
                        null,
                        lead.getCreatedAt(),
                        lead.getNextFollowUpAt()
                ))
                .toList();
    }
    public LeadDetailDTO getDetail(Long id, Long storeId) {

        Lead lead = leadRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado para esta tienda"));

        List<LeadActivityDTO> activities = leadActivityRepository
                .findByLeadIdOrderByCreatedAtDesc(id)
                .stream()
                .map(activity -> new LeadActivityDTO(
                        activity.getId(),
                        activity.getType().name(),
                        activity.getTitle(),
                        activity.getDescription(),
                        activity.getCreatedAt()
                ))
                .toList();

        List<SalesTaskDTO> tasks = salesTaskRepository
                .findByLeadIdOrderByDueAtAsc(id)
                .stream()
                .map(task -> new SalesTaskDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus().name(),
                        task.getPriority().name(),
                        task.getDueAt()
                ))
                .toList();
        List<LeadAuditLogDTO> auditLogs =
                leadAuditLogService.getByLead(
                        lead.getId(),
                        storeId
                );
        List<LeadTimelineItemDTO> timeline =
                leadTimelineService.getTimeline(
                        lead.getId(),
                        storeId
                );

        return new LeadDetailDTO(
                lead.getId(),
                lead.getNombre(),
                lead.getEmpresa(),
                null,
                lead.getWhatsapp(),
                lead.getStatus().name(),
                lead.getTemperature().name(),
                lead.getPriority().name(),
                lead.getScore(),
                null,
                lead.getProjectedValue(),
                activities,
                tasks,
                new ArrayList<>(),
                auditLogs,
                timeline
        );
    }
    
    private void recalculateScore(Lead lead) {
        try {
            LeadScoreResult result = leadScoringEngine.calculate(lead);

            lead.setScore(result.total());
            lead.setScoreBreakdown(
                    objectMapper.writeValueAsString(result.items())
            );

        } catch (Exception ex) {
            lead.setScore(0);
            lead.setScoreBreakdown("[]");
        }
    }
    
    public LeadScoreDTO getScore(Long leadId, Long storeId) {
        Lead lead = leadRepository.findByIdAndStoreId(leadId, storeId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado para esta tienda"));

        LeadScoreResult result = leadScoringEngine.calculate(lead);

        return new LeadScoreDTO(
                result.total(),
                result.items()
        );
    }
}

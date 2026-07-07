package com.webempresarial.store.crm.merge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webempresarial.store.crm.scoring.LeadScoreResult;
import com.webempresarial.store.crm.scoring.LeadScoringEngine;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.repository.LeadActivityRepository;
import com.webempresarial.store.repository.LeadAuditLogRepository;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.ProposalRepository;
import com.webempresarial.store.repository.SalesTaskRepository;
import com.webempresarial.store.service.crm.LeadAuditLogService;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LeadMergeService {

    private final LeadRepository leadRepository;
    private final LeadAuditLogService auditLogService;
    private final LeadActivityRepository leadActivityRepository;
    private final SalesTaskRepository salesTaskRepository;
    private final ProposalRepository proposalRepository;
    private final LeadAuditLogRepository auditLogRepository;
    private final LeadScoringEngine leadScoringEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeadMergeService(
            LeadRepository leadRepository,
            LeadAuditLogService auditLogService,
            LeadActivityRepository leadActivityRepository,
            SalesTaskRepository salesTaskRepository,
            ProposalRepository proposalRepository,
            LeadAuditLogRepository auditLogRepository,
            LeadScoringEngine leadScoringEngine
    ) {
        this.leadRepository = leadRepository;
        this.auditLogService = auditLogService;
        this.leadActivityRepository = leadActivityRepository;
        this.salesTaskRepository = salesTaskRepository;
        this.proposalRepository = proposalRepository;
        this.auditLogRepository = auditLogRepository;
        this.leadScoringEngine = leadScoringEngine;
    }
    private void recalculateScore(Lead lead, String actor) {
        try {
            int oldScore = lead.getScore();

            LeadScoreResult result = leadScoringEngine.calculate(lead);

            lead.setScore(result.total());
            lead.setScoreBreakdown(
                    objectMapper.writeValueAsString(result.items())
            );

            if (oldScore != result.total()) {
                auditLogService.record(
                        lead,
                        "SCORE_RECALCULATED",
                        "score",
                        oldScore,
                        result.total(),
                        actor
                );
            }

        } catch (Exception ex) {
            lead.setScore(0);
            lead.setScoreBreakdown("[]");
        }
    }
    private void moveRelations(Lead source, Lead target) {
        leadActivityRepository.findByLeadId(source.getId())
                .forEach(activity -> {
                    activity.setLead(target);
                    leadActivityRepository.save(activity);
                });

        salesTaskRepository.findByLeadId(source.getId())
                .forEach(task -> {
                    task.setLead(target);
                    salesTaskRepository.save(task);
                });
        proposalRepository.findByLeadId(source.getId())
        .forEach(proposal -> {
            proposal.setLead(target);
            proposalRepository.save(proposal);
        });
        auditLogRepository.findByLeadId(source.getId())
        .forEach(log -> {
            log.setLeadId(target.getId());
            auditLogRepository.save(log);
        });
        
    }

    @Transactional
    public LeadMergeResult merge(
            Long sourceLeadId,
            Long targetLeadId,
            Long storeId,
            MergeStrategy strategy,
            String actor
    ) {
        if (sourceLeadId.equals(targetLeadId)) {
            throw new RuntimeException("No puedes fusionar un lead consigo mismo");
        }

        Lead source = leadRepository.findByIdAndStoreId(sourceLeadId, storeId)
                .orElseThrow(() -> new RuntimeException("Lead origen no encontrado"));

        Lead target = leadRepository.findByIdAndStoreId(targetLeadId, storeId)
                .orElseThrow(() -> new RuntimeException("Lead destino no encontrado"));

        applyStrategy(source, target, strategy);
        moveRelations(source, target);

        target.setUpdatedAt(LocalDateTime.now());

        recalculateScore(target, actor);
        leadRepository.save(target);
        
        source.setMerged(true);
        source.setMergedIntoLeadId(target.getId());
        source.setMergedAt(LocalDateTime.now());
        source.setUpdatedAt(LocalDateTime.now());

        leadRepository.save(source);

        auditLogService.record(
                target,
                "LEAD_MERGED",
                "merge",
                "Lead #" + source.getId(),
                "Fusionado en Lead #" + target.getId(),
                actor
        );

        auditLogService.record(
                source,
                "MERGED_INTO",
                "merge",
                "Lead #" + source.getId(),
                "Lead #" + target.getId(),
                actor
        );

        return new LeadMergeResult(
                target.getId(),
                source.getId(),
                strategy,
                true,
                "Lead fusionado correctamente"
        );
    }

    private void applyStrategy(
            Lead source,
            Lead target,
            MergeStrategy strategy
    ) {
        if (strategy == MergeStrategy.KEEP_TARGET) {
            return;
        }

        if (strategy == MergeStrategy.KEEP_SOURCE) {
            target.setNombre(source.getNombre());
            target.setWhatsapp(source.getWhatsapp());
            target.setEmpresa(source.getEmpresa());
            target.setInstagram(source.getInstagram());
            target.setServicio(source.getServicio());
            target.setPresupuesto(source.getPresupuesto());
            target.setObjetivo(source.getObjetivo());
            target.setSource(source.getSource());
            return;
        }

        if (strategy == MergeStrategy.MERGE_NON_NULL) {
            if (isBlank(target.getNombre())) target.setNombre(source.getNombre());
            if (isBlank(target.getWhatsapp())) target.setWhatsapp(source.getWhatsapp());
            if (isBlank(target.getEmpresa())) target.setEmpresa(source.getEmpresa());
            if (isBlank(target.getInstagram())) target.setInstagram(source.getInstagram());
            if (isBlank(target.getServicio())) target.setServicio(source.getServicio());
            if (isBlank(target.getPresupuesto())) target.setPresupuesto(source.getPresupuesto());
            if (isBlank(target.getObjetivo())) target.setObjetivo(source.getObjetivo());
            if (isBlank(target.getSource())) target.setSource(source.getSource());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
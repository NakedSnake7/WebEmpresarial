package com.webempresarial.store.service.crm;

import com.webempresarial.store.dto.lead.LeadAuditLogDTO;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.LeadAuditLog;
import com.webempresarial.store.repository.LeadAuditLogRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadAuditLogService {

    private final LeadAuditLogRepository repository;

    public LeadAuditLogService(LeadAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(
            Lead lead,
            String action,
            String fieldName,
            Object oldValue,
            Object newValue,
            String actor
    ) {
        LeadAuditLog log = new LeadAuditLog();

        log.setLeadId(lead.getId());
        log.setStoreId(lead.getStore().getId());
        log.setAction(action);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue != null ? String.valueOf(oldValue) : null);
        log.setNewValue(newValue != null ? String.valueOf(newValue) : null);
        log.setActor(actor != null ? actor : "SYSTEM");

        repository.save(log);
    }
    public List<LeadAuditLogDTO> getByLead(Long leadId, Long storeId) {
        return repository.findByLeadIdAndStoreIdOrderByCreatedAtDesc(leadId, storeId)
                .stream()
                .map(log -> new LeadAuditLogDTO(
                        log.getId(),
                        log.getAction(),
                        log.getFieldName(),
                        log.getOldValue(),
                        log.getNewValue(),
                        log.getActor(),
                        log.getCreatedAt()
                ))
                .toList();
    }
}
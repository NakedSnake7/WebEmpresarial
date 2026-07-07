package com.webempresarial.store.repository;

import com.webempresarial.store.entity.LeadAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadAuditLogRepository extends JpaRepository<LeadAuditLog, Long> {

    List<LeadAuditLog> findByLeadIdAndStoreIdOrderByCreatedAtDesc(
            Long leadId,
            Long storeId
    );
    List<LeadAuditLog> findByLeadId(Long leadId);
}
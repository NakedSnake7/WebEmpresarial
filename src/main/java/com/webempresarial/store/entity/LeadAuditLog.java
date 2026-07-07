package com.webempresarial.store.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lead_audit_logs")
public class LeadAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long leadId;
    private Long storeId;

    private String action;
    private String fieldName;

    @Column(length = 1000)
    private String oldValue;

    @Column(length = 1000)
    private String newValue;

    private String actor;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }

    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
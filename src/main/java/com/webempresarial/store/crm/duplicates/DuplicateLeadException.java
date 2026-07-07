package com.webempresarial.store.crm.duplicates;

public class DuplicateLeadException extends RuntimeException {

    private final Long existingLeadId;
    private final DuplicateReason reason;

    public DuplicateLeadException(
            Long existingLeadId,
            DuplicateReason reason
    ) {
        super("Lead duplicado detectado por " + reason);
        this.existingLeadId = existingLeadId;
        this.reason = reason;
    }

    public Long getExistingLeadId() {
        return existingLeadId;
    }

    public DuplicateReason getReason() {
        return reason;
    }
}
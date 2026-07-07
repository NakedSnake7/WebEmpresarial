package com.webempresarial.store.service.crm;

import com.webempresarial.store.dto.lead.LeadTimelineItemDTO;
import com.webempresarial.store.repository.LeadActivityRepository;
import com.webempresarial.store.repository.LeadAuditLogRepository;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class LeadTimelineService {

    private final LeadActivityRepository activityRepository;
    private final LeadAuditLogRepository auditLogRepository;

    public LeadTimelineService(
            LeadActivityRepository activityRepository,
            LeadAuditLogRepository auditLogRepository
    ) {
        this.activityRepository = activityRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<LeadTimelineItemDTO> getTimeline(Long leadId, Long storeId) {

        var activities = activityRepository
                .findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream()
                .map(activity -> new LeadTimelineItemDTO(
                        "activity-" + activity.getId(),
                        "ACTIVITY",
                        activity.getTitle(),
                        activity.getDescription(),
                        iconForActivity(activity.getType().name()),
                        "primary",
                        "SYSTEM",
                        activity.getCreatedAt()
                ));

        var audits = auditLogRepository
                .findByLeadIdAndStoreIdOrderByCreatedAtDesc(leadId, storeId)
                .stream()
                .map(log -> new LeadTimelineItemDTO(
                        "audit-" + log.getId(),
                        "AUDIT",
                        titleForAudit(log.getAction(), log.getFieldName()),
                        formatAuditDescription(
                                log.getFieldName(),
                                log.getOldValue(),
                                log.getNewValue()
                        ),
                        iconForAudit(log.getAction()),
                        "dark",
                        log.getActor(),
                        log.getCreatedAt()
                ));

        return Stream.concat(activities, audits)
                .sorted(Comparator.comparing(LeadTimelineItemDTO::createdAt).reversed())
                .toList();
    }

    private String titleForAudit(String action, String fieldName) {
        if ("STATUS_CHANGED".equals(action)) {
            return "Estado actualizado";
        }

        if ("LEAD_CREATED".equals(action)) {
            return "Lead creado";
        }

        return fieldName != null ? "Cambio en " + fieldName : action;
    }

    private String formatAuditDescription(
            String fieldName,
            String oldValue,
            String newValue
    ) {
        if (oldValue == null) {
            return newValue;
        }

        return oldValue + " → " + newValue;
    }

    private String iconForActivity(String type) {
        return switch (type) {
            case "LEAD_CREATED" -> "🟢";
            case "STATUS_CHANGED" -> "🔄";
            case "NOTE_ADDED" -> "📝";
            default -> "📌";
        };
    }

    private String iconForAudit(String action) {
        return switch (action) {
            case "LEAD_CREATED" -> "🟢";
            case "STATUS_CHANGED" -> "✏️";
            default -> "🧾";
        };
    }
}
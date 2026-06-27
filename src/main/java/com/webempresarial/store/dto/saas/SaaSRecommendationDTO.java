package com.webempresarial.store.dto.saas;

public class SaaSRecommendationDTO {

    private String type;
    private String title;
    private String message;
    private String actionLabel;
    private String actionUrl;
    private String severity;

    public SaaSRecommendationDTO() {
    }

    public SaaSRecommendationDTO(
            String type,
            String title,
            String message,
            String actionLabel,
            String actionUrl,
            String severity
    ) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.actionLabel = actionLabel;
        this.actionUrl = actionUrl;
        this.severity = severity;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public String getSeverity() {
        return severity;
    }
}
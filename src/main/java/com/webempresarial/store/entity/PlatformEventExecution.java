package com.webempresarial.store.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "platform_event_executions")
public class PlatformEventExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventName;

    private String sourceModule;

    private LocalDateTime occurredAt;

    private LocalDateTime finishedAt;

    private boolean success;

    private long totalDurationMs;

    private int totalListeners;

    @OneToMany(
            mappedBy = "execution",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlatformEventListenerExecution> listeners = new ArrayList<>();
    
    private String correlationId;
    private String executionId;

    private String parentExecutionId;
    private String spanId;

    public String getParentExecutionId() { return parentExecutionId; }
    public void setParentExecutionId(String parentExecutionId) { this.parentExecutionId = parentExecutionId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public void addListener(PlatformEventListenerExecution listener) {
        listener.setExecution(this);
        this.listeners.add(listener);
    }

    public Long getId() { return id; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public long getTotalDurationMs() { return totalDurationMs; }
    public void setTotalDurationMs(long totalDurationMs) { this.totalDurationMs = totalDurationMs; }

    public int getTotalListeners() { return totalListeners; }
    public void setTotalListeners(int totalListeners) { this.totalListeners = totalListeners; }

    public List<PlatformEventListenerExecution> getListeners() { return listeners; }
    public void setListeners(List<PlatformEventListenerExecution> listeners) { this.listeners = listeners; }
}
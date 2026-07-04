package com.webempresarial.store.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "execution_spans")
public class ExecutionSpan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String correlationId;
    private String executionId;
    private String parentExecutionId;
    private String spanId;

    private String type;
    private String name;
    private String source;

    private boolean success;

    @Column(length = 1000)
    private String message;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    private long durationMs;

    public Long getId() { return id; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getParentExecutionId() { return parentExecutionId; }
    public void setParentExecutionId(String parentExecutionId) { this.parentExecutionId = parentExecutionId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
package com.webempresarial.store.dto.platform;

import java.util.ArrayList;
import java.util.List;

public class ExecutionTraceNodeDTO {

    private String executionId;
    private String parentExecutionId;
    private String spanId;

    private String type;
    private String title;
    private String source;
    private String status;
    private String occurredAt;

    private long durationMs;

    private List<ExecutionTraceNodeDTO> children = new ArrayList<>();

    public void addChild(ExecutionTraceNodeDTO child) {
        children.add(child);
    }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getParentExecutionId() { return parentExecutionId; }
    public void setParentExecutionId(String parentExecutionId) { this.parentExecutionId = parentExecutionId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public List<ExecutionTraceNodeDTO> getChildren() { return children; }
    public void setChildren(List<ExecutionTraceNodeDTO> children) { this.children = children; }
}
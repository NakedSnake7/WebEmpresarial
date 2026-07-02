package com.webempresarial.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "automation_execution_actions")
public class AutomationExecutionAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actionName;

    private boolean success;

    @Column(length = 1000)
    private String message;

    private long durationMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id")
    private AutomationExecution execution;

    public Long getId() { return id; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public AutomationExecution getExecution() { return execution; }
    public void setExecution(AutomationExecution execution) { this.execution = execution; }
}
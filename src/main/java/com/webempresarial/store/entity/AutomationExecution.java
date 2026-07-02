package com.webempresarial.store.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "automation_executions")
public class AutomationExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String triggerName;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private boolean success;

    private int totalActions;

    private long totalDurationMs;

    @OneToMany(
            mappedBy = "execution",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AutomationExecutionAction> actions = new ArrayList<>();

    public void addAction(AutomationExecutionAction action) {
        action.setExecution(this);
        this.actions.add(action);
    }

    public Long getId() { return id; }

    public String getTriggerName() { return triggerName; }
    public void setTriggerName(String triggerName) { this.triggerName = triggerName; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getTotalActions() { return totalActions; }
    public void setTotalActions(int totalActions) { this.totalActions = totalActions; }

    public long getTotalDurationMs() { return totalDurationMs; }
    public void setTotalDurationMs(long totalDurationMs) { this.totalDurationMs = totalDurationMs; }

    public List<AutomationExecutionAction> getActions() { return actions; }
    public void setActions(List<AutomationExecutionAction> actions) { this.actions = actions; }
}
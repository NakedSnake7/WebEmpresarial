package com.webempresarial.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "platform_event_listener_executions")
public class PlatformEventListenerExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String listenerName;

    private boolean success;

    private long durationMs;

    @Column(length = 1000)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id")
    private PlatformEventExecution execution;

    public Long getId() { return id; }

    public String getListenerName() { return listenerName; }
    public void setListenerName(String listenerName) { this.listenerName = listenerName; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public PlatformEventExecution getExecution() { return execution; }
    public void setExecution(PlatformEventExecution execution) { this.execution = execution; }
}
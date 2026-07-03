package com.webempresarial.store.feature.runtime;

import com.webempresarial.store.feature.PlatformModule;
import com.webempresarial.store.feature.PlatformModuleDescriptor;

import java.time.LocalDateTime;

public class ModuleRuntime {

    private final PlatformModule module;
    private final PlatformModuleDescriptor descriptor;

    private ModuleRuntimeStatus status = ModuleRuntimeStatus.DISCOVERED;
    private LocalDateTime discoveredAt = LocalDateTime.now();
    private LocalDateTime loadedAt;
    private LocalDateTime bootedAt;
    private String errorMessage;
    
    private long bootDurationMs;

    private long eventsProcessed;

    private long automationsExecuted;

    private long healthChecksExecuted;

    private long restartCount;

    private long failureCount;

    private long warningCount;

    private long lastExecutionTimeMs;
    
    public void incrementRestartCount() {
        restartCount++;
    }

    public void setLastExecutionTime(long ms) {
        lastExecutionTimeMs = ms;
    }
    
    public void incrementEvents() {
        eventsProcessed++;
    }

    public void incrementAutomations() {
        automationsExecuted++;
    }

    public void incrementHealthChecks() {
        healthChecksExecuted++;
    }

    public void incrementFailures() {
        failureCount++;
    }

    public void incrementWarnings() {
        warningCount++;
    }

    public void markBootCompleted(long durationMs) {
        this.bootDurationMs = durationMs;
        this.bootedAt = LocalDateTime.now();
        this.status = ModuleRuntimeStatus.BOOTED;
    }

    public ModuleRuntime(
            PlatformModule module,
            PlatformModuleDescriptor descriptor
    ) {
        this.module = module;
        this.descriptor = descriptor;
    }

    public PlatformModule module() {
        return module;
    }

    public PlatformModuleDescriptor descriptor() {
        return descriptor;
    }

    public String name() {
        return descriptor.getName();
    }

    public ModuleRuntimeStatus status() {
        return status;
    }

    public LocalDateTime discoveredAt() {
        return discoveredAt;
    }

    public LocalDateTime loadedAt() {
        return loadedAt;
    }

    public LocalDateTime bootedAt() {
        return bootedAt;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public void markLoaded() {
        this.status = ModuleRuntimeStatus.LOADED;
        this.loadedAt = LocalDateTime.now();
    }

    public void markBooted() {
        this.status = ModuleRuntimeStatus.BOOTED;
        this.bootedAt = LocalDateTime.now();
    }

    public void markFailed(Exception ex) {
        this.status = ModuleRuntimeStatus.FAILED;
        this.errorMessage = ex.getMessage();
    }
    public long bootDurationMs() {
        return bootDurationMs;
    }

    public long eventsProcessed() {
        return eventsProcessed;
    }

    public long automationsExecuted() {
        return automationsExecuted;
    }

    public long healthChecksExecuted() {
        return healthChecksExecuted;
    }

    public long restartCount() {
        return restartCount;
    }

    public long failureCount() {
        return failureCount;
    }

    public long warningCount() {
        return warningCount;
    }

    public long lastExecutionTimeMs() {
        return lastExecutionTimeMs;
    }
}
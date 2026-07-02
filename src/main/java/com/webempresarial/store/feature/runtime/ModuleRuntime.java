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
}
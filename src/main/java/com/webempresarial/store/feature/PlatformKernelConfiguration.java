package com.webempresarial.store.feature;

import com.webempresarial.store.feature.runtime.ModuleLifecycleManager;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class PlatformKernelConfiguration {

    private final ModuleLifecycleManager lifecycleManager;

    public PlatformKernelConfiguration(
            ModuleLifecycleManager lifecycleManager
    ) {
        this.lifecycleManager = lifecycleManager;
    }

    @PostConstruct
    public void initializePlatform() {
        lifecycleManager.startPlatform();
    }
}
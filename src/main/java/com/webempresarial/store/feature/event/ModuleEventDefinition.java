package com.webempresarial.store.feature.event;

public record ModuleEventDefinition(
        String name,
        String description,
        String sourceModule
) {}
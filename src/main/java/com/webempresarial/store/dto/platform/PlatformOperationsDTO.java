package com.webempresarial.store.dto.platform;

public record PlatformOperationsDTO(
        PlatformConsoleDTO console,
        long automationExecutions,
        long eventExecutions
) {}
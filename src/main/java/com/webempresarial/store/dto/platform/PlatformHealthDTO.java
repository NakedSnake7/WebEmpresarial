package com.webempresarial.store.dto.platform;

public record PlatformHealthDTO(
        String name,
        String status,
        String message,
        String checkedAt
) {}
package com.webempresarial.store.dto.platform;

import java.util.List;

public record PlatformOperationsDTO(
        PlatformConsoleDTO console,
        long automationExecutions,
        long eventExecutions,
        List<PlatformTimelineItemDTO> timeline
) {}
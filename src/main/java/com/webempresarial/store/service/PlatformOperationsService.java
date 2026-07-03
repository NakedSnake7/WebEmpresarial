package com.webempresarial.store.service;

import com.webempresarial.store.dto.platform.PlatformOperationsDTO;
import com.webempresarial.store.repository.AutomationExecutionRepository;
import com.webempresarial.store.repository.PlatformEventExecutionRepository;

import org.springframework.stereotype.Service;

@Service
public class PlatformOperationsService {

    private final PlatformExplorerService platformExplorerService;
    private final AutomationExecutionRepository automationExecutionRepository;
    private final PlatformEventExecutionRepository platformEventExecutionRepository;
    private final PlatformTimelineService timelineService;

    public PlatformOperationsService(
            PlatformExplorerService platformExplorerService,
            AutomationExecutionRepository automationExecutionRepository,
            PlatformEventExecutionRepository platformEventExecutionRepository,
            PlatformTimelineService timelineService
    ) {
        this.platformExplorerService = platformExplorerService;
        this.automationExecutionRepository = automationExecutionRepository;
        this.platformEventExecutionRepository = platformEventExecutionRepository;
        this.timelineService = timelineService;
    }

    public PlatformOperationsDTO operations() {
        return new PlatformOperationsDTO(
                platformExplorerService.console(),
                automationExecutionRepository.count(),
                platformEventExecutionRepository.count(),
                timelineService.latest()
        );
    }
}
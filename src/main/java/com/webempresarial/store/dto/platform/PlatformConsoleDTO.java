package com.webempresarial.store.dto.platform;

import java.util.List;

public record PlatformConsoleDTO(
        int modules,
        int features,
        int dashboardWidgets,
        int sidebarSections,
        int permissions,
        int automations,
        List<PlatformModuleDTO> moduleList,
        List<PlatformHealthDTO> healthResults
        
) {}
package com.webempresarial.store.dto.sidebar;

import java.util.List;

public record SidebarSectionDTO(
        String title,
        String icon,
        List<SidebarItemDTO> items
) {}
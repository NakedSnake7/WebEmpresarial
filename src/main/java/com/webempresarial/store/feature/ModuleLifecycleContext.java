package com.webempresarial.store.feature;

import com.webempresarial.store.feature.registry.AutomationRegistry;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.feature.registry.MarketplaceRegistry;
import com.webempresarial.store.feature.registry.ModuleRegistry;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.feature.registry.EventRegistry;
import com.webempresarial.store.feature.registry.PermissionRegistry;

public record ModuleLifecycleContext(
        PlatformKernel platformKernel,
        ModuleRegistry moduleRegistry,
        SidebarRegistry sidebarRegistry,
        DashboardRegistry dashboardRegistry,
        MarketplaceRegistry marketplaceRegistry,
        EventRegistry eventRegistry,
        PermissionRegistry permissionRegistry,
        AutomationRegistry automationRegistry
) {}
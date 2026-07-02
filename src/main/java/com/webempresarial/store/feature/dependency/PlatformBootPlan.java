package com.webempresarial.store.feature.dependency;

import com.webempresarial.store.feature.runtime.ModuleRuntime;

import java.util.List;

public record PlatformBootPlan(
        List<ModuleRuntime> bootOrder
) {}
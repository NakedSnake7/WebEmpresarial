package com.webempresarial.store.feature.dependency;

import java.util.List;

public record ModuleDependencyReport(
        boolean valid,
        List<String> missingDependencies,
        List<String> cycles
) {}
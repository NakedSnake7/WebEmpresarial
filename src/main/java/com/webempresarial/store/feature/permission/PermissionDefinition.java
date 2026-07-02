package com.webempresarial.store.feature.permission;

import com.webempresarial.store.model.Feature;

public record PermissionDefinition(
        String code,
        String name,
        String description,
        Feature feature
) {}
package com.webempresarial.store.feature.automation;

import com.webempresarial.store.model.Feature;

public record AutomationDefinition(
        String code,
        String name,
        String description,
        Feature feature,
        String trigger
) {}
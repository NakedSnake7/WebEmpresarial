package com.webempresarial.store.feature.automation;

import java.util.Map;

public record AutomationContext(
        String trigger,
        Object payload,
        Map<String, Object> metadata
) {}
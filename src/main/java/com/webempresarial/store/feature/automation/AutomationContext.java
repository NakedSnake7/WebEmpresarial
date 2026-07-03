package com.webempresarial.store.feature.automation;

import com.webempresarial.store.feature.runtime.ExecutionContext;

import java.util.Map;

public record AutomationContext(
        String trigger,
        ExecutionContext executionContext,
        Object payload,
        Map<String, Object> metadata
) {}
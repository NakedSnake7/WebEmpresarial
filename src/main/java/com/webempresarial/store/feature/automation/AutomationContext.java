package com.webempresarial.store.feature.automation;

import com.webempresarial.store.feature.runtime.ExecutionContext;
import com.webempresarial.store.feature.runtime.ExecutionScope;

import java.util.Map;

public record AutomationContext(
        String trigger,
        ExecutionContext executionContext,
        ExecutionScope scope,
        Object payload,
        Map<String, Object> metadata
) {}
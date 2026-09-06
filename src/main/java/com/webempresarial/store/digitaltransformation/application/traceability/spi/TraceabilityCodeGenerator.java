package com.webempresarial.store.digitaltransformation.application.traceability.spi;

import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;

public interface TraceabilityCodeGenerator {

    String generateForExternalReference(
            TraceabilityNodeType nodeType,
            String externalReference
    );
}
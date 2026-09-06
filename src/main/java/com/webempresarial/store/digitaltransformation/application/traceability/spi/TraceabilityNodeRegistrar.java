package com.webempresarial.store.digitaltransformation.application.traceability.spi;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityOrigin;

public interface TraceabilityNodeRegistrar {

    TraceabilityNode register(
            TransformationProject project,
            String nodeCode,
            TraceabilityNodeType nodeType,
            TraceabilityOrigin origin,
            String title,
            String description,
            String externalReference,
            String externalEntityType,
            boolean requiresReview
    );
}
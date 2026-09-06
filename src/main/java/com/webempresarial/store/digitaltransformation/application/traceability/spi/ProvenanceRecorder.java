package com.webempresarial.store.digitaltransformation.application.traceability.spi;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;

public interface ProvenanceRecorder {

    ProvenanceRecord recordNodeAction(
            TransformationProject project,
            TraceabilityNode node,
            ProvenanceAction action,
            TraceabilityOrigin origin,
            String actor,
            String actorType,
            String processReference,
            String explanation
    );

    ProvenanceRecord recordLinkAction(
            TransformationProject project,
            TraceabilityLink link,
            ProvenanceAction action,
            TraceabilityOrigin origin,
            String actor,
            String actorType,
            String processReference,
            String explanation
    );
}
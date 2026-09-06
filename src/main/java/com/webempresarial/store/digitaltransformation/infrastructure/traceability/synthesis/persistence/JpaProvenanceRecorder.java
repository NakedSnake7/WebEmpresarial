package com.webempresarial.store.digitaltransformation.infrastructure.traceability.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Component;

@Component
public class JpaProvenanceRecorder
        implements ProvenanceRecorder {

    private final ProvenanceRecordRepository repository;

    public JpaProvenanceRecorder(
            ProvenanceRecordRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public ProvenanceRecord recordNodeAction(
            TransformationProject project,
            TraceabilityNode node,
            ProvenanceAction action,
            TraceabilityOrigin origin,
            String actor,
            String actorType,
            String processReference,
            String explanation
    ) {
        return repository.save(
                ProvenanceRecord.forNode(
                        project,
                        node,
                        action,
                        origin,
                        actor,
                        actorType,
                        processReference,
                        explanation
                )
        );
    }

    @Override
    public ProvenanceRecord recordLinkAction(
            TransformationProject project,
            TraceabilityLink link,
            ProvenanceAction action,
            TraceabilityOrigin origin,
            String actor,
            String actorType,
            String processReference,
            String explanation
    ) {
        return repository.save(
                ProvenanceRecord.forLink(
                        project,
                        link,
                        action,
                        origin,
                        actor,
                        actorType,
                        processReference,
                        explanation
                )
        );
    }
}
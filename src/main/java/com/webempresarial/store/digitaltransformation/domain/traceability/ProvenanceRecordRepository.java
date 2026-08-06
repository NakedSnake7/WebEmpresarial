package com.webempresarial.store.digitaltransformation.domain.traceability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProvenanceRecordRepository
        extends JpaRepository<ProvenanceRecord, Long> {

    List<ProvenanceRecord>
    findAllByProjectIdOrderByRecordedAtAsc(
            Long projectId
    );

    List<ProvenanceRecord>
    findAllByTraceabilityNodeIdOrderByRecordedAtAsc(
            Long traceabilityNodeId
    );

    List<ProvenanceRecord>
    findAllByTraceabilityLinkIdOrderByRecordedAtAsc(
            Long traceabilityLinkId
    );

    List<ProvenanceRecord>
    findAllByProjectIdAndActorOrderByRecordedAtAsc(
            Long projectId,
            String actor
    );
}
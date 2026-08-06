package com.webempresarial.store.digitaltransformation.application.traceability;

import com.webempresarial.store.digitaltransformation.application.shared.TraceabilityNodeNotFoundException;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TraceabilityAccessService {

    private final TraceabilityNodeRepository nodeRepository;

    public TraceabilityAccessService(
            TraceabilityNodeRepository nodeRepository
    ) {
        this.nodeRepository = nodeRepository;
    }

    public TraceabilityNode requireNode(
            Long storeId,
            Long nodeId
    ) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }

        if (nodeId == null || nodeId <= 0) {
            throw new IllegalArgumentException(
                    "El nodeId debe ser válido"
            );
        }

        return nodeRepository
                .findByIdAndProjectStoreId(
                        nodeId,
                        storeId
                )
                .orElseThrow(() ->
                        new TraceabilityNodeNotFoundException(
                                nodeId,
                                storeId
                        )
                );
    }
}
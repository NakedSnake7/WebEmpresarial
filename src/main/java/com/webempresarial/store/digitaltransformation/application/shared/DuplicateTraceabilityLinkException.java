package com.webempresarial.store.digitaltransformation.application.shared;

public class DuplicateTraceabilityLinkException
        extends RuntimeException {

    public DuplicateTraceabilityLinkException(
            Long sourceNodeId,
            Long targetNodeId
    ) {
        super(
                "Ya existe una relación entre los nodos " +
                sourceNodeId +
                " y " +
                targetNodeId
        );
    }
}
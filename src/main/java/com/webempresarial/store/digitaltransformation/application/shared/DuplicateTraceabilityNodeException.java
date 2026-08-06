package com.webempresarial.store.digitaltransformation.application.shared;

public class DuplicateTraceabilityNodeException
        extends RuntimeException {

    public DuplicateTraceabilityNodeException(
            Long projectId,
            String nodeCode
    ) {
        super(
                "Ya existe un nodo de trazabilidad con código " +
                nodeCode +
                " en el proyecto " +
                projectId
        );
    }
}
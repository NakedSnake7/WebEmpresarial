package com.webempresarial.store.digitaltransformation.application.shared;

public class TraceabilityNodeNotFoundException
        extends RuntimeException {

    public TraceabilityNodeNotFoundException(
            Long nodeId,
            Long storeId
    ) {
        super(
                "No se encontró el nodo de trazabilidad " +
                nodeId +
                " para el store " +
                storeId
        );
    }
}
package com.webempresarial.store.digitaltransformation.application.shared;

public class TransformationSourceNotFoundException
        extends RuntimeException {

    public TransformationSourceNotFoundException(
            Long sourceId,
            Long storeId
    ) {
        super(
                "No se encontró el documento fuente " +
                sourceId + " para el store " + storeId
        );
    }
}
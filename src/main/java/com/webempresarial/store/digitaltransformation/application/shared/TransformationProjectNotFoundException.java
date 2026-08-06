package com.webempresarial.store.digitaltransformation.application.shared;

public class TransformationProjectNotFoundException
        extends RuntimeException {

    public TransformationProjectNotFoundException(
            Long projectId,
            Long storeId
    ) {
        super(
                "No se encontró el proyecto de transformación " +
                projectId + " para el store " + storeId
        );
    }
}
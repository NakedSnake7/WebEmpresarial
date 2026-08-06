package com.webempresarial.store.digitaltransformation.application.shared;

public class DuplicateTransformationProjectException
        extends RuntimeException {

    public DuplicateTransformationProjectException(
            Long storeId,
            String code
    ) {
        super(
                "Ya existe un proyecto de transformación con código " +
                code + " para el store " + storeId
        );
    }
}
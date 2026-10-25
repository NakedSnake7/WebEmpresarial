package com.webempresarial.store.exceptions;

public class CustomDomainValidationException
        extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public CustomDomainValidationException(String message) {
        super(message);
    }
}

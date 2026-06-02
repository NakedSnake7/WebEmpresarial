package com.webempresarial.store.exceptions;

public class FeatureNotAllowedException extends RuntimeException {
	
	private static final long serialVersionUID = 20260601L;

    public FeatureNotAllowedException(String message) {
        super(message);
    }
}
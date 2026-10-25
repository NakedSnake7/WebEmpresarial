package com.webempresarial.store.exceptions;

public class StoreNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StoreNotFoundException(String domain) {
        super("Tienda no encontrada para dominio: " + domain);
    }
}

package com.webempresarial.store.exceptions;

import com.webempresarial.store.model.Feature;

public class FeatureLockedException extends RuntimeException {

    private final Feature feature;

    public FeatureLockedException(Feature feature) {
        super("Feature bloqueada: " + feature.name());
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }
}
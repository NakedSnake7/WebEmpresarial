package com.webempresarial.store.feature;

public interface PlatformModule {

    ModuleDefinition definition();

    default String name() {
        return getClass().getSimpleName();
    }
}
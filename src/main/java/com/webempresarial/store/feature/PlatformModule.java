package com.webempresarial.store.feature;

public interface PlatformModule {

    ModuleDefinition definition();

    default void boot(ModuleLifecycleContext context) {
    }

    default String name() {
        return getClass().getSimpleName();
    }
}
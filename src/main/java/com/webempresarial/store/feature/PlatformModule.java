package com.webempresarial.store.feature;

public interface PlatformModule {

    PlatformModuleDescriptor descriptor();

    default void boot(ModuleLifecycleContext context) {
    }

    default String name() {
        return getClass().getSimpleName();
    }
}
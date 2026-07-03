package com.webempresarial.store.feature.event;

public interface PlatformEventListener {

    boolean supports(String eventName);

    void handle(PlatformEvent event);
}
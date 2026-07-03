package com.webempresarial.store.feature.event;

import java.util.ArrayList;
import java.util.List;

public class PlatformEventReport {

    private final PlatformEvent event;
    private final List<EventExecutionResult> listeners = new ArrayList<>();

    public PlatformEventReport(PlatformEvent event) {
        this.event = event;
    }

    public void add(EventExecutionResult result) {
        listeners.add(result);
    }

    public PlatformEvent event() {
        return event;
    }

    public List<EventExecutionResult> listeners() {
        return List.copyOf(listeners);
    }

    public boolean success() {
        return listeners.stream()
                .allMatch(result -> result.success());
    }
}
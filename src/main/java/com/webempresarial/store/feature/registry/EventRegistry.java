package com.webempresarial.store.feature.registry;

import com.webempresarial.store.feature.event.ModuleEventDefinition;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventRegistry {

    private final List<ModuleEventDefinition> events = new ArrayList<>();

    public void register(ModuleEventDefinition event) {
        if (event != null) {
            events.add(event);
        }
    }

    public List<ModuleEventDefinition> all() {
        return List.copyOf(events);
    }
}
package com.webempresarial.store.feature.registry;

import com.webempresarial.store.feature.permission.PermissionDefinition;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PermissionRegistry {

    private final List<PermissionDefinition> permissions = new ArrayList<>();

    public void register(PermissionDefinition permission) {
        if (permission != null) {
            permissions.add(permission);
        }
    }

    public List<PermissionDefinition> all() {
        return List.copyOf(permissions);
    }
}
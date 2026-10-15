package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminPermission;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentAdminAuthorizationService {

    public boolean hasPermission(
            AdminPermission permission
    ) {

        if (permission == null) {
            return false;
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        String requiredAuthority =
                permission.authority();

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        requiredAuthority.equals(
                                authority.getAuthority()
                        )
                );
    }

    public void requirePermission(
            AdminPermission permission
    ) {

        if (!hasPermission(permission)) {
            throw new AccessDeniedException(
                    "Missing admin permission: "
                    + permission.name()
            );
        }
    }
}

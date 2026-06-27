package com.webempresarial.store.config;

import org.springframework.security.core.Authentication; 
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SecurityViewAdvice {

    @ModelAttribute("isSuperAdmin")
    public boolean isSuperAdmin(Authentication auth) {
        return hasRole(auth, "ROLE_SUPER_ADMIN");
    }

    @ModelAttribute("isStoreAdmin")
    public boolean isStoreAdmin(Authentication auth) {
        return hasRole(auth, "ROLE_STORE_ADMIN");
    }

    @ModelAttribute("isStoreStaff")
    public boolean isStoreStaff(Authentication auth) {
        return hasRole(auth, "ROLE_STORE_STAFF");
    }

    private boolean hasRole(Authentication auth, String role) {

        if (auth == null) {
            return false;
        }

        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
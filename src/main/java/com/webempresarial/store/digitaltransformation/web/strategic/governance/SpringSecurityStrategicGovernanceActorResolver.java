package com.webempresarial.store.digitaltransformation.web.strategic.governance;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReviewerType;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SpringSecurityStrategicGovernanceActorResolver
        implements StrategicGovernanceActorResolver {

    @Override
    public StrategicGovernanceActor resolve() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new IllegalStateException(
                    "No existe un usuario autenticado para governance"
            );
        }

        String reviewer =
                normalizeReviewer(
                        authentication.getName()
                );

        if (hasAuthority(
                authentication,
                "ROLE_SUPER_ADMIN"
        )) {
            return humanConsultant(
                    reviewer
            );
        }

        if (hasAuthority(
                authentication,
                "ROLE_STORE_ADMIN"
        )) {
            return humanConsultant(
                    reviewer
            );
        }

        /*
         * STORE_STAFF puede acceder al panel administrativo,
         * pero no recibe autoridad estratégica implícita.
         */
        if (hasAuthority(
                authentication,
                "ROLE_STORE_STAFF"
        )) {
            throw new IllegalStateException(
                    "El usuario autenticado no está autorizado para decisiones de governance"
            );
        }

        throw new IllegalStateException(
                "El usuario autenticado no posee una identidad válida para governance"
        );
    }

    private static StrategicGovernanceActor humanConsultant(
            String reviewer
    ) {
        return new StrategicGovernanceActor(
                reviewer,
                StrategicSynthesisReviewerType.HUMAN_CONSULTANT
        );
    }

    private static boolean hasAuthority(
            Authentication authentication,
            String authority
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .filter(Objects::nonNull)
                .map(GrantedAuthority::getAuthority)
                .anyMatch(
                        authority::equals
                );
    }

    private static String normalizeReviewer(
            String value
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "La identidad del reviewer autenticado es inválida"
            );
        }

        return value.trim();
    }
}